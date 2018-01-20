package pl.edu.mimuw.cloudatlas.agent.modules;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.tools.picocli.CommandLine;
import org.cfg4j.provider.ConfigurationProvider;
import pl.edu.mimuw.cloudatlas.agent.framework.*;
import pl.edu.mimuw.cloudatlas.agent.messages.*;
import pl.edu.mimuw.cloudatlas.model.*;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.*;

@Module(value = "GossipModule", unique = true, dependencies = {"TimerModule", "CommunicationModule", "ZMIModule"})
public class GossipModule extends ModuleBase {

    private static Logger LOGGER = LogManager.getLogger(GossipModule.class);

    public static final int UPDATE_ROOT = 1;
    public static final int SET_FALLBACK_CONTACTS = 2;
    public static final int INIT_GOSSIP = 3;
    public static final int EXCHANGE_INFO = 4;
    public static final int UPDATE_ZMIS = 5;

    private static final int RANDOM_STRATEGY = 1;
    private static final int RANDOM_EXPONENTIAL_STRATEGY = 2;
    private static final int ROUND_ROBIN_STRATEGY = 3;

    private ZMI root = null;
    private Strategy strategy;
    private Long delay;
    private PathName agentId;
    private Long nextId = 0L;
    private Random random = new Random();
    private Set<ValueContact> fallbackContacts = new HashSet<>();


    @Handler(UPDATE_ROOT)
    private final MessageHandler<?> h1 = new MessageHandler<GenericMessage<ZMI>>() {
        @Override
        protected void handle(GenericMessage<ZMI> message) {
            try {
                LOGGER.debug("UPDATE_ROOT: IN: " + message.toString());

                root = message.getData();
            } catch (Exception e) {
                LOGGER.error("UPDATE_ROOT: " + e.getMessage(), e);
            }
        }
    };

    @Handler(SET_FALLBACK_CONTACTS)
    private final MessageHandler<?> h2 = new MessageHandler<GenericMessage<ValueContact[]>>() {
        @Override
        protected void handle(GenericMessage<ValueContact[]> message) {
            try {
                LOGGER.debug("SET_FALLBACK_CONTACTS: IN: " + message.toString());

                fallbackContacts = new HashSet<>(Arrays.asList(message.getData()));
            } catch (Exception e) {
                LOGGER.error("SET_FALLBACK_CONTACTS: " + e.getMessage(), e);
            }
        }
    };

    @Handler(INIT_GOSSIP)
    private final MessageHandler<?> h3 = new MessageHandler<Message>() {

        @Override
        protected void handle(Message message) {
            try {
                LOGGER.debug("INIT_GOSSIP: IN: " + message.toString());

                Message timerMsg = new EmptyMessage();
                timerMsg.setAddress(new Address(GossipModule.class, INIT_GOSSIP));
                sendMessage(new Address(TimerModule.class, TimerModule.SCHEDULE),
                        new TimerMessage(nextId++, getName(), System.currentTimeMillis(), delay, timerMsg));
                LOGGER.debug("INIT_GOSSIP: OUT: " + timerMsg.toString());

                if (root != null) {
                    PathName selectedZone = strategy.selectZone(agentId);

                    ZMI zmi = ZMIModule.zmiByID(root, selectedZone);
                    List<ZMI> zmiWithContacts = new ArrayList<>();
                    if (zmi != null) {
                        for (ZMI son : zmi.getSons()) {
                            ValueString sonRep = (ValueString) son.getAttributes().getOrNull(ZMIModule.REP);
                            ValueList sonContacts = (ValueList) son.getAttributes().getOrNull(ZMIModule.CONTACTS);

                            if (!agentId.equals(new PathName(sonRep.getValue())) && !sonContacts.isEmpty()) {
                                zmiWithContacts.add(son);
                            }
                        }
                    }

                    ValueContact contact;
                    if (!zmiWithContacts.isEmpty()) {
                        ZMI toGossip = zmiWithContacts.get(random.nextInt(zmiWithContacts.size()));
                        List<Value> valueList = ((ValueList) toGossip.getAttributes().getOrNull(ZMIModule.CONTACTS)).getValue();
                        Value selectedValue = new ArrayList<>(valueList).get(random.nextInt(valueList.size()));
                        contact = (ValueContact) selectedValue;
                    } else if (!fallbackContacts.isEmpty()) {
                        LOGGER.warn("INIT_GOSSIP: using fallback contacts");

                        contact = new ArrayList<>(fallbackContacts).get(random.nextInt(fallbackContacts.size()));
                        selectedZone = PathName.getLCA(agentId, contact.getName());
                        zmi = ZMIModule.zmiByID(root, selectedZone);

                    } else {
                        LOGGER.warn("INIT_GOSSIP: no fallback contacts");
                        return;
                    }

                    GenericMessage<?> msg = new GenericMessage<>(new ZMIInfo(root, agentId, selectedZone));
                    msg.setAddress(new Address(GossipModule.class, EXCHANGE_INFO));
                    Message netMsg = new NetworkMessage<>(contact.getAddress(), contact.getPort(), msg);
                    Address address = new Address(CommunicationModule.class, CommunicationModule.SEND);
                    sendMessage(address, netMsg);

                    LOGGER.debug("INIT_GOSSIP: OUT: " + msg.toString());
                }
            } catch (Exception e) {
                LOGGER.error("INIT_GOSSIP: " + e.getMessage(), e);
            }
        }
    };

    @Handler(EXCHANGE_INFO)
    private final MessageHandler<?> h4 = new MessageHandler<FromNetworkMessage<ZMIInfo>>() {
        @Override
        protected void handle(FromNetworkMessage<ZMIInfo> message) {
            try {
                LOGGER.debug("EXCHANGE_INFO: IN: " + message.toString() + message.getData().toString());

                ZMIInfo otherInfo = message.getData();
                ZMIInfo myInfo = new ZMIInfo(root, agentId, otherInfo.getSelectedZMI());
                myInfo.setTimestampSnd(message.getTimestampSnd());
                myInfo.setTimestampRcv(message.getTimestampRcv());

                if (!otherInfo.hasTimestamps()) {
                    GenericMessage<?> genericMessage = new GenericMessage<>(myInfo);
                    genericMessage.setAddress(new Address(GossipModule.class, EXCHANGE_INFO));
                    Message netMsg = new NetworkMessage<>(message.getSenderAddress(), message.getSenderPort(), genericMessage);
                    Address address = new Address(CommunicationModule.class, CommunicationModule.SEND);
                    sendMessage(address, netMsg);

                    LOGGER.debug("EXCHANGE_INFO: OUT: " + netMsg.toString());
                } else {

                    Long delay = ZMIInfo.computeDelay(otherInfo, myInfo);

                    List<PathName> requests = otherInfo.freshnessDiff(myInfo, delay);
                    List<PathName> responses = myInfo.freshnessDiff(otherInfo, delay);

                    GossipData gossipData = new GossipData(root, requests, responses, delay);
                    GenericMessage<?> msg = new GenericMessage<>(gossipData);
                    msg.setAddress(new Address(GossipModule.class, UPDATE_ZMIS));
                    Address address = new Address(CommunicationModule.class, CommunicationModule.SEND);
                    Message netMsg = new NetworkMessage<>(message.getSenderAddress(), message.getSenderPort(), msg);
                    sendMessage(address, netMsg);

                    LOGGER.debug("EXCHANGE_INFO: OUT: " + netMsg.toString());
                }
            } catch (Exception e) {
                LOGGER.error("EXCHANGE_INFO: " + e.getMessage(), e);
            }
        }
    };

    @Handler(UPDATE_ZMIS)
    private final MessageHandler<?> h5 = new MessageHandler<FromNetworkMessage<GossipData>>() {
        @Override
        protected void handle(FromNetworkMessage<GossipData> message) {
            try {
                LOGGER.debug("UPDATE_ZMIS: IN: " + message.toString() + message.getData().toString());

                if (!message.getData().getRequests().isEmpty()) {
                    GossipData gossipData = new GossipData(root, new ArrayList<>(), message.getData().requests, message.getData().delay);

                    GenericMessage<?> msg = new GenericMessage<>(gossipData);
                    msg.setAddress(new Address(GossipModule.class, UPDATE_ZMIS));
                    Address address = new Address(CommunicationModule.class, CommunicationModule.SEND);
                    Message netMsg = new NetworkMessage<>(message.getSenderAddress(), message.getSenderPort(), msg);
                    sendMessage(address, netMsg);
                    LOGGER.debug("UPDATE_ZMIS: OUT: " + netMsg.toString());
                }

                Long timeOffset = message.getTimestampSnd() + message.getData().delay - message.getTimestampRcv();

                Address address = new Address(ZMIModule.class, ZMIModule.UPDATE_ATTRIBUTES);
                for (AttributesMap attrs : message.getData().getData()) {

                    ZMI zmi = ZMIModule.zmiByID(root, new PathName(((ValueString) attrs.get(ZMIModule.ID)).getValue()));

                    if (zmi != null) {
                        Long issued1 = ((ValueInt) attrs.get(ZMIModule.ISSUED)).getValue();
                        Long issued2 = ((ValueInt) zmi.getAttributes().get(ZMIModule.ISSUED)).getValue();
                        if (issued1 <= (issued2 + timeOffset)) {
                            continue;
                        }
                    }

                    Message msg = new GenericMessage<>(attrs);
                    sendMessage(address, msg);
                    LOGGER.debug("UPDATE_ZMIS: OUT: " + msg.toString());
                }

            } catch (Exception e) {
                LOGGER.error("UPDATE_ZMIS: " + e.getMessage(), e);
            }
        }
    };

    @Override
    public void initialize(ConfigurationProvider configurationProvider) {

        try {
            LOGGER.debug("INITIALIZE");

            agentId = new PathName(configurationProvider.getProperty("Agent.agentId", String.class));
            delay = configurationProvider.getProperty("Agent.GossipModule.delay", Long.class);
            Integer strategyId = configurationProvider.getProperty("Agent.GossipModule.strategy", Integer.class);

            switch (strategyId) {
                case RANDOM_STRATEGY:
                    strategy = new RandomStrategy();
                    break;
                case RANDOM_EXPONENTIAL_STRATEGY:
                    strategy = new RandomExponentialStrategy();
                    break;
                case ROUND_ROBIN_STRATEGY:
                    strategy = new RoundRobinStrategy();
                    break;
                default:
                    throw new Exception("Wrong strategy id");
            }

            fallbackContacts.addAll(Arrays.asList(
                    new ValueContact(new PathName("/uw/cpu1"), InetAddress.getByName("localhost"), 1338),
                    new ValueContact(new PathName("/uw/cpu2"), InetAddress.getByName("localhost"), 1339)));

        } catch (Exception e) {
            LOGGER.error("INITIALIZE: " + e.getMessage(), e);
        }

    }

    /*********************************************************************************************
                                            Subclasses
     ********************************************************************************************/

    public static class GossipData implements Serializable {
        private List<PathName> requests;
        private List<AttributesMap> data = new ArrayList<>();
        private Long delay;

        public GossipData(ZMI root, List<PathName> requests, List<PathName> responses, Long delay) {
            this.delay = delay;
            this.requests = requests;
            for (PathName pathName : responses) {
                ZMI zmi = ZMIModule.zmiByID(root, pathName);
                if (zmi != null)
                    data.add(zmi.getAttributes());
            }
        }

        public Long getDelay() {
            return delay;
        }

        public List<PathName> getRequests() {
            return requests;
        }

        public List<AttributesMap> getData() {
            return data;
        }

        @Override
        public String toString() {
            return "GossipData{" +
                    "requests=" + requests +
                    ", data=" + data +
                    ", delay=" + delay +
                    '}';
        }
    }


    public static class ZMIInfo implements Serializable {

        private static class Info implements Serializable {
            private PathName id;
            private PathName res;
            private Long issued;

            Info(PathName id, PathName res, Long issued) {
                this.id = id;
                this.res = res;
                this.issued = issued;
            }

            @Override
            public String toString() {
                return "Info{" +
                        "id=" + id +
                        ", res=" + res +
                        ", issued=" + issued +
                        '}';
            }
        }

        private PathName agentId;
        private PathName selectedZMI;
        private List<Info> zones = new ArrayList<>();
        private Long timestampSnd;
        private Long timestampRcv;

        public ZMIInfo(ZMI root, PathName agentId, PathName selectedZMI) {
            collectZoneInfo(ZMIModule.zmiByID(root, selectedZMI));
            this.selectedZMI = selectedZMI;
            this.agentId = agentId;
        }

        private void collectZoneInfo(ZMI zmi) {
            if (zmi.getFather() != null) {
                collectZoneInfo(zmi.getFather());
            }

            for (ZMI son : zmi.getSons()) {
                PathName id = new PathName(((ValueString) son.getAttributes().getOrNull(ZMIModule.ID)).getValue());
                PathName res = new PathName(((ValueString) son.getAttributes().getOrNull(ZMIModule.REP)).getValue());
                Long issued = ((ValueInt) son.getAttributes().getOrNull(ZMIModule.ISSUED)).getValue();
                zones.add(new Info(id, res, issued));
            }
        }

        public List<PathName> freshnessDiff(ZMIInfo zmiInfo, Long delay) {

            Long timeOffset = timestampSnd + delay - timestampRcv;

            Map<PathName, Info> zonesMap = new HashMap<>();
            for (Info info : zones) {
                zonesMap.put(info.id, info);
            }

            for (Info info : zmiInfo.zones) {
                Info myInfo = zonesMap.get(info.id);
                if (myInfo != null && (info.issued >= (myInfo.issued + timeOffset) || myInfo.res.equals(zmiInfo.agentId))) {
                    zonesMap.remove(info.id);
                }
            }
            return new ArrayList<>(zonesMap.keySet());
        }

        public static Long computeDelay(ZMIInfo zmiInfo1, ZMIInfo zmiInfo2) {
            return ((zmiInfo2.timestampRcv - zmiInfo1.timestampSnd) - (zmiInfo2.timestampSnd - zmiInfo1.timestampRcv))/2;
        }

        public PathName getSelectedZMI() {
            return selectedZMI;
        }

        public Boolean hasTimestamps() {
            return timestampSnd != null && timestampRcv != null;
        }

        public void setTimestampSnd(Long timestampSnd) {
            this.timestampSnd = timestampSnd;
        }

        public void setTimestampRcv(Long timestampRcv) {
            this.timestampRcv = timestampRcv;
        }

        @Override
        public String toString() {
            return "ZMIInfo{" +
                    "agentId=" + agentId +
                    ", selectedZMI=" + selectedZMI +
                    ", zones=" + zones +
                    ", timestampSnd=" + timestampSnd +
                    ", timestampRcv=" + timestampRcv +
                    '}';
        }
    }

    private abstract class Strategy {
        public abstract PathName selectZone(PathName pathName);
    }

    private class RandomStrategy extends Strategy{

        @Override
        public PathName selectZone(PathName pathName) {
            List<String> zones = pathName.getComponents();
            return new PathName(zones.subList(0, random.nextInt(zones.size())));
        }
    }

    private class RandomExponentialStrategy extends Strategy {

        @Override
        public PathName selectZone(PathName pathName) {
            List<String> zones = pathName.getComponents();

            PathName result = new PathName("/");
            for (String zone : zones) {
                if (random.nextDouble() > 0.5) {
                    return result;
                }
                result.levelDown(zone);
            }
            return result;
        }
    }

    private class RoundRobinStrategy extends Strategy {
        private Integer next = 0;

        @Override
        public PathName selectZone(PathName pathName) {
            List<String> zones = pathName.getComponents();
            next = (next + 1) % zones.size();
            return new PathName(zones.subList(0, next));
        }
    }
}

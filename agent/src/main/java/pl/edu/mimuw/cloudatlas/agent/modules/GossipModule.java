package pl.edu.mimuw.cloudatlas.agent.modules;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cfg4j.provider.ConfigurationProvider;
import pl.edu.mimuw.cloudatlas.agent.framework.*;
import pl.edu.mimuw.cloudatlas.agent.messages.*;
import pl.edu.mimuw.cloudatlas.model.*;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.*;

//TODO
// Add strategies for choosing zone
// Add time synchronization
// Add one more handler in between to have time info

@Module(value = "GossipModule", unique = true, dependencies = {"TimerModule", "CommunicationModule", "ZMIModule"})
public class GossipModule extends ModuleBase {

    private static Logger LOGGER = LogManager.getLogger(GossipModule.class);

    public static final int UPDATE_ROOT = 1;
    public static final int SET_FALLBACK_CONTACTS = 2;
    public static final int INIT_GOSSIP = 3;
    public static final int EXCHANGE_INFO = 4;
    public static final int UPDATE_ZMIS = 5;

    private ZMI root = null;
    private Long delay;
    private PathName agentId;
    private Integer port;
    private Long nextId = 0L;
    private Random random = new Random();
    private Set<ValueContact> fallbackContacts = new HashSet<>();


    @Handler(UPDATE_ROOT)
    private final MessageHandler<?> h1 = new MessageHandler<ZMIRequestMessage>() {
        @Override
        protected void handle(ZMIRequestMessage message) {
            try {
                LOGGER.debug("UPDATE_ROOT: IN: " + message.toString());

                root = message.getZmi();
            } catch (Exception e) {
                LOGGER.error("UPDATE_ROOT: " + e.getMessage(), e);
            }
        }
    };

    @Handler(SET_FALLBACK_CONTACTS)
    private final MessageHandler<?> h2 = new MessageHandler<ContactsMessage>() {
        @Override
        protected void handle(ContactsMessage message) {
            try {
                LOGGER.debug("SET_FALLBACK_CONTACTS: IN: " + message.toString());

                fallbackContacts = message.getContacts();
            } catch (Exception e) {
                LOGGER.error("SET_FALLBACK_CONTACTS: " + e.getMessage(), e);
            }
        }
    };

    @Handler(INIT_GOSSIP)
    private final MessageHandler<?> h3 = new MessageHandler<Message>() {
        private PathName selectRandomZMI(PathName pathName) {
            List<String> zones = pathName.getComponents();
            return new PathName(zones.subList(0, random.nextInt(zones.size())));
        }

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
                    PathName selectedZone = selectRandomZMI(agentId);

                    ZMI zmi = ZMIModule.zmiByID(root, selectedZone);
                    List<ZMI> zmiWithContacts = new ArrayList<>();
                    for (ZMI son : zmi.getSons()) {
                        ValueString sonRep = (ValueString) son.getAttributes().getOrNull(ZMIModule.REP);
                        ValueSet sonContacts = (ValueSet) son.getAttributes().getOrNull(ZMIModule.CONTACTS);

                        if (!agentId.equals(new PathName(sonRep.getValue())) && !sonContacts.isEmpty()) {
                            zmiWithContacts.add(son);
                        }
                    }

                    ValueContact contact;
                    if (!zmiWithContacts.isEmpty()) {
                        ZMI toGossip = zmiWithContacts.get(random.nextInt(zmiWithContacts.size()));
                        Set<Value> valuesSet = ((ValueSet) toGossip.getAttributes().getOrNull(ZMIModule.CONTACTS)).getValue();
                        Value selectedValue = new ArrayList<>(valuesSet).get(random.nextInt(valuesSet.size()));
                        contact = (ValueContact) selectedValue;
                    } else {
                        if (fallbackContacts.isEmpty()) {
                            throw new HandlerException("No fallback contacts");
                        }

                        contact = new ArrayList<>(fallbackContacts).get(random.nextInt(fallbackContacts.size()));
                        selectedZone = PathName.getLCA(agentId, contact.getName());
                        zmi = ZMIModule.zmiByID(root, selectedZone);

                        LOGGER.warn("INIT_GOSSIP: using fallback contacts");
                    }

                    Message msg = new GossipFreshnessMessage(selectedZone, new ZMIInfo(zmi), agentId, InetAddress.getLocalHost(), port);
                    msg.setAddress(new Address(GossipModule.class, EXCHANGE_INFO));
                    Message netMsg = new NetworkMessage(contact.getAddress(), contact.getPort(), msg);
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
    private final MessageHandler<?> h4 = new MessageHandler<GossipFreshnessMessage>() {
        @Override
        protected void handle(GossipFreshnessMessage message) {
            try {
                LOGGER.debug("EXCHANGE_INFO: IN: " + message.toString());

                ZMIInfo myInfo = new ZMIInfo(ZMIModule.zmiByID(root, message.getSelectedZone()));
                ZMIInfo otherInfo = message.getInfo();

                List<PathName> requests = otherInfo.freshnessDiff(myInfo, agentId);
                List<PathName> responses =  myInfo.freshnessDiff(otherInfo, message.getSenderId());

                List<AttributesMap> data = new ArrayList<>();
                for (PathName pathName : responses) {
                    ZMI zmi = ZMIModule.zmiByID(root, pathName);
                    if (zmi != null)
                        data.add(zmi.getAttributes());
                }

                Message msg = new GossipMessage(requests, data, InetAddress.getLocalHost(), port);
                msg.setAddress(new Address(GossipModule.class, UPDATE_ZMIS));
                Address address = new Address(CommunicationModule.class, CommunicationModule.SEND);
                Message netMsg = new NetworkMessage(message.getSender(), message.getSenderPort(), msg);
                sendMessage(address, netMsg);

                LOGGER.debug("EXCHANGE_INFO: OUT: " + msg.toString());

            } catch (Exception e) {
                LOGGER.error("EXCHANGE_INFO: " + e.getMessage(), e);
            }
        }
    };

    @Handler(UPDATE_ZMIS)
    private final MessageHandler<?> h5 = new MessageHandler<GossipMessage>() {
        @Override
        protected void handle(GossipMessage message) {
            try {
                LOGGER.debug("UPDATE_ZMIS: IN: " + message.toString());

                if (!message.getRequests().isEmpty()) {
                    List<AttributesMap> data = new ArrayList<>();
                    for (PathName pathName : message.getRequests()) {
                        ZMI zmi = ZMIModule.zmiByID(root, pathName);
                        if (zmi != null)
                            data.add(zmi.getAttributes());
                    }
                    Message msg = new GossipMessage(new ArrayList<>(), data, InetAddress.getLocalHost(), port);
                    msg.setAddress(new Address(GossipModule.class, UPDATE_ZMIS));
                    Address address = new Address(CommunicationModule.class, CommunicationModule.SEND);
                    Message netMsg = new NetworkMessage(message.getSender(), message.getSenderPort(), msg);
                    sendMessage(address, netMsg);

                    LOGGER.debug("UPDATE_ZMIS: OUT: " + msg.toString());
                }

                Address address = new Address(ZMIModule.class, ZMIModule.UPDATE_ATTRIBUTES);
                for (AttributesMap attrs : message.getData()) {
                    PathName id = new PathName(((ValueString) attrs.get(ZMIModule.ID)).getValue());
                    ZMI zmi = ZMIModule.zmiByID(root, id);

                    if (zmi != null) {
                        Long issued1 = ((ValueInt) attrs.get(ZMIModule.ISSUED)).getValue();
                        Long issued2 = ((ValueInt) zmi.getAttributes().get(ZMIModule.ISSUED)).getValue();
                        if (issued1 <= issued2) {
                            continue;
                        }
                    }

                    Message msg = new AttributesMapMessage(attrs, id);
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
            port = configurationProvider.getProperty("Agent.port", Integer.class);

            fallbackContacts.addAll(Arrays.asList(
                    new ValueContact(new PathName("/uw/khaki13"), InetAddress.getLocalHost(), 19901),
                    new ValueContact(new PathName("/uw/pink02"), InetAddress.getLocalHost(), 19902)));

        } catch (Exception e) {

        }

    }

    /*********************************************************************************************
                                            Subclasses
     ********************************************************************************************/

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

        private List<Info> zones = new ArrayList<>();

        public ZMIInfo(ZMI zmi) {
            collectZoneInfo(zmi);
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

        public List<PathName> freshnessDiff(ZMIInfo zmiInfo, PathName agentId) {

            Map<PathName, Info> zonesMap = new HashMap<>();
            for (Info info : zones) {
                zonesMap.put(info.id, info);
            }

            for (Info info : zmiInfo.zones) {
                Info myInfo = zonesMap.get(info.id);
                if (myInfo != null && (info.issued >= myInfo.issued || myInfo.res.equals(agentId))) {
                    zonesMap.remove(info.id);
                }
            }

            return new ArrayList<>(zonesMap.keySet());
        }

        @Override
        public String toString() {
            return "ZMIInfo{" +
                    "zones=" + zones +
                    '}';
        }
    }
}

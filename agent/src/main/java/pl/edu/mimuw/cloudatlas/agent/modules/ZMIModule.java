package pl.edu.mimuw.cloudatlas.agent.modules;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cfg4j.provider.ConfigurationProvider;
import pl.edu.mimuw.cloudatlas.agent.framework.*;
import pl.edu.mimuw.cloudatlas.model.*;

import java.net.InetAddress;
import java.util.*;

@Module(value = "ZMIModule", unique = true, dependencies = {"InterpreterModule"})
public class ZMIModule extends ModuleBase {

    private static Logger LOGGER = LogManager.getLogger(ZMIModule.class);

    public static final int UPDATE_ATTRIBUTES = 1;
    public static final int GET_ATTRIBUTES = 2;
    public static final int GET_ZONES = 3;
    public static final int INSTALL_QUERY = 4;
    public static final int UNINSTALL_QUERY = 5;

    public static final String ID = "id";
    public static final String REP = "rep";
    public static final String ISSUED = "issued";
    public static final String CONTACTS = "contacts";

    private PathName agentId;
    private Integer port;
    private ZMI root;

    @Handler(UPDATE_ATTRIBUTES)
    private final MessageHandler<?> h1 = new MessageHandler<GenericMessage<AttributesMap>>() {
        @Override
        protected void handle(GenericMessage<AttributesMap> message) {
            try {
                LOGGER.debug("UPDATE_ATTRIBUTES: IN:" + message.toString());

                ValueString zoneId = (ValueString) message.getData().getOrNull(ID);

                if(zoneId == null) {
                    throw new HandlerException("No key in attributes map");
                }

                PathName pathName = new PathName(zoneId.getValue());

                ZMI zmi = zmiByID(root, pathName);

                if (zmi == null) {
                    ZMI parentZmi = zmiByID(root, pathName.levelUp());
                    if (parentZmi != null) {
                        zmi = new ZMI(parentZmi);
                        parentZmi.addSon(zmi);
                    } else {
                        throw new HandlerException("Zmi not found");
                    }
                }

                for (Map.Entry<Attribute, Value> entry : message.getData()) {
                    zmi.getAttributes().addOrChange(entry);
                }

                zmi.getAttributes().addOrChange(ISSUED, new ValueInt(System.currentTimeMillis()));

                if (!zmi.equals(root)) {
                    ZMI father = zmi.getFather();
                    Address address = new Address(InterpreterModule.class, InterpreterModule.EXEC_QUERIES);
                    Message msg = new GenericMessage<>(father);
                    sendMessage(address, msg);
                    LOGGER.debug("UPDATE_ATTRIBUTES: OUT: " + msg.toString());

                } else {
                    Address address = new Address(GossipModule.class, GossipModule.UPDATE_ROOT);
                    Message msg = new GenericMessage<>(root);
                    sendMessage(address, msg);
                    LOGGER.debug("UPDATE_ATTRIBUTES: OUT: " + msg.toString());
                }
            } catch (Exception e) {
                LOGGER.error("UPDATE_ATTRIBUTES: " + e.getMessage(), e);
            }
        }
    };

    @Handler(GET_ATTRIBUTES)
    private final MessageHandler<?> h2 = new MessageHandler<GenericMessage<PathName>>() {
        @Override
        protected void handle(GenericMessage<PathName> message) {

            LOGGER.debug("GET_ATTRIBUTES: IN: " + message.toString());

            ZMI zmi = zmiByID(root, message.getData());
            if (zmi != null) {
                AttributesMap attributesMap = zmi.getAttributes();

                Address address = new Address(RMIModule.class, RMIModule.RESPOND_ATTRIBUTES);
                Message msg = new GenericMessage<>(attributesMap);
                sendMessage(address, msg);
                LOGGER.debug("GET_ATTRIBUTES: OUT: " + msg.toString());
            }
        }
    };

    @Handler(GET_ZONES)
    private final MessageHandler<?> h3 = new MessageHandler<EmptyMessage>() {
        @Override
        protected void handle(EmptyMessage message) {

            LOGGER.debug("GET_ZONES: IN: " + message.toString());

            Address address = new Address(RMIModule.class, RMIModule.RESPOND_ZONES);
            PathName[] zones = (PathName[]) getZones(root).toArray();
            Message msg = new GenericMessage<>(zones);
            sendMessage(address, msg);
            LOGGER.debug("GET_ZONES: OUT: " + msg.toString());

        }

        private Set<PathName> getZones(ZMI zmi) {
            Set<PathName> pathNames = new HashSet<>();
            pathNames.add(new PathName(((ValueString) zmi.getAttributes().get(ID)).getValue()));

            if (!zmi.getSons().isEmpty())
                for(ZMI son : zmi.getSons())
                    pathNames.addAll(getZones(son));

            return pathNames;
        }
    };

    @Override
    public void initialize(ConfigurationProvider configurationProvider) {
        agentId = new PathName(configurationProvider.getProperty("Agent.agentId", String.class));
        port = configurationProvider.getProperty("Agent.port", Integer.class);

        ZMI singletonZone = initZMI(agentId);
    }


    private ZMI initZMI(PathName currentZone) {
        ZMI zone = null;

        try {

            LOGGER.debug(currentZone.toString());

            if (currentZone.equals(PathName.ROOT)) {
                zone = new ZMI();
                root = zone;
            } else {
                ZMI parent = initZMI(currentZone.levelUp());
                zone = new ZMI(parent);
                parent.addSon(zone);
            }

            Set<Value> contacts = new HashSet<>();
            contacts.add(new ValueContact(agentId, InetAddress.getLocalHost(), port));

            zone.getAttributes().add(ID, new ValueString(currentZone.toString()));
            zone.getAttributes().add(REP, new ValueString(agentId.toString()));
            zone.getAttributes().add(ISSUED, new ValueInt(System.currentTimeMillis()));
            zone.getAttributes().add(CONTACTS, new ValueSet(contacts, TypePrimitive.CONTACT));

        } catch (Exception e) {}

        return zone;
    }

    public static ZMI zmiByID(ZMI zmi, PathName id) {
        Boolean found;
        List<String> nameList = id.getComponents();
        PathName pathName = new PathName("");
        for(String name : nameList) {
            pathName = pathName.levelDown(name);
            found = false;

            for (ZMI son : zmi.getSons()) {
                PathName sonId = new PathName(((ValueString) son.getAttributes().get(ID)).getValue());
                if (pathName.equals(sonId)) {
                    zmi = son;
                    found = true;
                    break;
                }
            }

            if (!found) {
                return  null;
            }
        }
        return zmi;
    }
}

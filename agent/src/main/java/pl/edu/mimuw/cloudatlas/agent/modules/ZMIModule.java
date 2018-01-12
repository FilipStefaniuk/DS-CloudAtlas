package pl.edu.mimuw.cloudatlas.agent.modules;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cfg4j.provider.ConfigurationProvider;
import pl.edu.mimuw.cloudatlas.agent.framework.*;
import pl.edu.mimuw.cloudatlas.agent.messages.*;
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
    private final MessageHandler<?> h1 = new MessageHandler<AttributesMapMessage>() {
        @Override
        protected void handle(AttributesMapMessage message) {
            try {
                LOGGER.debug("UPDATE_ATTRIBUTES: IN:" + message.toString());

                ZMI zmi = zmiByID(root, message.getPathName());

                if (zmi == null) {
                    ZMI parentZmi = zmiByID(root, message.getPathName().levelUp());
                    if (parentZmi != null) {
                        zmi = new ZMI(parentZmi);
                        parentZmi.addSon(zmi);
                    } else {
                        throw new HandlerException("Zmi not found");
                    }
                }

                for (Map.Entry<Attribute, Value> entry : message.getAttributesMap()) {
                    zmi.getAttributes().addOrChange(entry);
                }

                if (!zmi.equals(root)) {
                    ZMI father = zmi.getFather();
                    PathName pathName = new PathName(((ValueString) father.getAttributes().get(ID)).getValue());
                    Address responseAddress = message.getAddress();
                    Address address = new Address(InterpreterModule.class, InterpreterModule.EXEC_QUERIES);
                    sendMessage(address, new ZMIRequestMessage(responseAddress, pathName, father));
                } else {
                    Address address = new Address(GossipModule.class, GossipModule.UPDATE_ROOT);
                    sendMessage(address, new ZMIRequestMessage(message.getAddress(), new PathName(""), root));
                }
            } catch (Exception e) {
                LOGGER.error("UPDATE_ATTRIBUTES: " + e.getMessage(), e);
            }
        }
    };

    @Handler(GET_ATTRIBUTES)
    private final MessageHandler<?> h2 = new MessageHandler<PathNameRequestMessage>() {
        @Override
        protected void handle(PathNameRequestMessage message) {

            LOGGER.debug("GET_ATTRIBUTES: IN: " + message.toString());

            ZMI zmi = zmiByID(root, message.getPathName());
            if (zmi != null) {
                AttributesMap attributesMap = zmi.getAttributes();
                sendMessage(message.getResponseAddress(), new AttributesMapMessage(attributesMap, message.getPathName()));
            }
        }
    };

    @Handler(GET_ZONES)
    private final MessageHandler<?> h3 = new MessageHandler<RequestMessage>() {
        @Override
        protected void handle(RequestMessage message) {

            LOGGER.debug("GET_ZONES: IN: " + message.toString());

            Message msg = new ZonesMessage(getZones(root));
            sendMessage(message.getResponseAddress(), msg);

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
        List<String> nameList = id.getComponents();
        PathName pathName = new PathName("");
        for(String name : nameList) {
            pathName = pathName.levelDown(name);
            for (ZMI son : zmi.getSons()) {
                PathName sonId = new PathName(((ValueString) son.getAttributes().get(ID)).getValue());
                if (pathName.equals(sonId)) {
                    zmi = son;
                    break;
                }
                return null;
            }
        }
        return zmi;
    }
}

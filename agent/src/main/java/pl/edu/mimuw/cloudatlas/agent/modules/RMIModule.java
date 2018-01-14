package pl.edu.mimuw.cloudatlas.agent.modules;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cfg4j.provider.ConfigurationProvider;
import pl.edu.mimuw.cloudatlas.agent.AgentInterface;
import pl.edu.mimuw.cloudatlas.agent.framework.*;
import pl.edu.mimuw.cloudatlas.agent.messages.*;
import pl.edu.mimuw.cloudatlas.model.*;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

@Module(value = "RMIModule", unique = true)//, dependencies = {"ZMIModule", "GossipModule"})
public class RMIModule extends ModuleBase {

    private static Logger LOGGER = LogManager.getLogger(RMIModule.class);

    public static final int RESPOND_ATTRIBUTES = 1;
    public static final int RESPOND_ZONES = 2;

    private Long rmiTimeout;
    private String rmiName;
    private ConcurrentMap<PathName, BlockingQueue<AttributesMap>> attributesQueue = new ConcurrentHashMap<>();
    private BlockingQueue<Set<PathName>> zonesQueue = new LinkedBlockingQueue<>();

    private AgentInterface server = new AgentInterface() {

        @Override
        public void setAttributes(PathName pathName, AttributesMap attributesMap) throws RemoteException {
            Address address = new Address(ZMIModule.class, ZMIModule.ADD_OR_CHANGE_ATTRIBUTES);
            Message msg = new GenericMessage<>(attributesMap);
            sendMessage(address, msg);

            LOGGER.debug("SET_ATTRIBUTES: OUT:" + msg.toString());
        }

        @Override
        public AttributesMap getAttributes(PathName pathName) throws RemoteException {
            try {

                Address address = new Address(ZMIModule.class, ZMIModule.GET_ATTRIBUTES);
                Address responseAddress = new Address(RMIModule.class, RESPOND_ATTRIBUTES);
                Message msg = new GenericMessage<>(pathName);
                sendMessage(address, msg);
                LOGGER.debug("GET_ATTRIBUTES: OUT:" + msg.toString());


                BlockingQueue<AttributesMap> queue = attributesQueue.get(pathName);
                if (queue == null) {
                    queue = new LinkedBlockingQueue<>();
                    attributesQueue.put(pathName, queue);
                }

                return queue.poll(rmiTimeout, TimeUnit.MILLISECONDS);

            } catch (Exception e) {
                LOGGER.error("GET_ATTRIBUTES: " + e.getMessage(), e);
                throw new RemoteException();
            }
        }

        @Override
        public void installQuery(Attribute attribute, ValueQuery value) throws RemoteException {
            try {
                AttributesMap attributesMap = new AttributesMap();
                attributesMap.addOrChange(attribute, value);

                Address address = new Address(VerifierModule.class, VerifierModule.VERIFY_QUERIES);
                Message msg = new GenericMessage<>(attributesMap);
                sendMessage(address, msg);
                LOGGER.debug("INSTALL_QUERY: OUT:" + msg.toString());

            } catch (Exception e) {
                LOGGER.error("INSTALL_QUERY: " + e.getMessage(), e);
                throw new RemoteException();
            }
        }

//        @Override
//        public void uninstallQuery(Attribute attribute) throws RemoteException {
//        }

        @Override
        public Set<PathName> getAgentZones() throws RemoteException {

            try {

                Address address = new Address(ZMIModule.class, ZMIModule.GET_ZONES);
                Message msg = new EmptyMessage();
                sendMessage(address, msg);

                LOGGER.debug("GET_AGENT_ZONES: OUT: " + msg.toString());

                return zonesQueue.poll(rmiTimeout, TimeUnit.MILLISECONDS);

            } catch (Exception e) {
                LOGGER.error("GET_AGENT_ZONES: OUT: " + e.getMessage(), e);
                throw new RemoteException();
            }
        }

        @Override
        public void setFallbackContacts(Set<ValueContact> contacts) throws RemoteException {
            try {
                LOGGER.debug("SET_FALLBACK_CONTACTS");

                Address address = new Address(ZMIModule.class, GossipModule.SET_FALLBACK_CONTACTS);
                ValueContact[] valueContacts = (ValueContact[]) contacts.toArray();
                Message msg = new GenericMessage<>(valueContacts);
                sendMessage(address, msg);
                LOGGER.debug("SET_FALLBACK_CONTACTS: OUT: " + msg.toString());
            } catch (Exception e) {
                LOGGER.error("SET_FALLBACK_CONTACTS: " + e.getMessage(), e);
            }
        }
    };

    @Handler(RESPOND_ATTRIBUTES)
    private final MessageHandler<?> h1 = new MessageHandler<GenericMessage<AttributesMap>>() {
        @Override
        protected void handle(GenericMessage<AttributesMap> message) {
            try {

                LOGGER.debug("RESPOND_ATTRIBUTES: IN: " + message.toString());

                PathName pathName = new PathName(((ValueString)message.getData().get(ZMIModule.ID)).getValue());
                BlockingQueue<AttributesMap> queue = attributesQueue.get(pathName);
                if (queue != null) {
                    queue.put(message.getData());
                }

            } catch (Exception e) {
                LOGGER.error("RESPOND_ATTRIBUTES: " + e.getMessage(), e);
            }
        }
    };

    @Handler(RESPOND_ZONES)
    private final MessageHandler<?> h2 = new MessageHandler<GenericMessage<ValueSet>>() {
        @Override
        protected void handle(GenericMessage<ValueSet> message) {
            try {

                LOGGER.debug("RESPOND_ZONES: IN:" + message.toString());

                Set<PathName> pathNames = new HashSet<>();
                for (Value value : message.getData().getValue()) {
                    pathNames.add(new PathName(((ValueString) value).getValue()));
                }
                zonesQueue.put(new HashSet<>(pathNames));

            } catch (Exception e) {
                LOGGER.error("RESPOND_ZONES: " + e.getMessage(), e);
            }
        }
    };

    @Override
    public void initialize(ConfigurationProvider configurationProvider) {

        String agentId = configurationProvider.getProperty("Agent.agentId", String.class);
        rmiName = configurationProvider.getProperty("Agent.RMIModule.rmiName", String.class);
        rmiTimeout = configurationProvider.getProperty("Agent.RMIModule.rmiTimeout", Long.class);

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            AgentInterface stub = (AgentInterface) UnicastRemoteObject.exportObject(server, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(rmiName, stub);

        } catch (RemoteException e) {
            LOGGER.error("RMI module error", e);
            throw new IllegalStateException();
        }
    }
}
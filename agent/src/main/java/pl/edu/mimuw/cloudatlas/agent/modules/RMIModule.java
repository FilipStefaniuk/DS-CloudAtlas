package pl.edu.mimuw.cloudatlas.agent.modules;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cfg4j.provider.ConfigurationProvider;
import pl.edu.mimuw.cloudatlas.agent.AgentInterface;
import pl.edu.mimuw.cloudatlas.agent.framework.*;
import pl.edu.mimuw.cloudatlas.agent.messages.AttributesMapMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.ContactsMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.PathNameRequestMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.ZonesMessage;
import pl.edu.mimuw.cloudatlas.model.*;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

//TODO
// Install uninstall query
// fix  attributes queue
@Module(value = "RMIModule", unique = true, dependencies = {"ZMIModule", "GossipModule"})
public class RMIModule extends ModuleBase {

    private static Logger LOGGER = LogManager.getLogger(RMIModule.class);

    private static final String AGENT_ID_CONFIG = "Agent.agentId";
    private static final String RMI_TIMEOUT_CONFIG = "Agent.RMIModule.rmiTimeout";

    private static final int RESPOND_ATTRIBUTES = 1;
    private static final int RESPOND_ZONES = 2;

    private Long rmiTimeout;
    private BlockingQueue<AttributesMap> attributesQueue = new LinkedBlockingQueue<>();
    private BlockingQueue<Set<PathName>> zonesQueue = new LinkedBlockingQueue<>();

    private AgentInterface server = new AgentInterface() {

        @Override
        public void setAttributes(PathName pathName, AttributesMap attributesMap) throws RemoteException {
            Address address = new Address(ZMIModule.class, ZMIModule.UPDATE_ATTRIBUTES);
            AttributesMapMessage attributesMapMessage = new AttributesMapMessage(attributesMap, pathName);
            sendMessage(address, attributesMapMessage);

            LOGGER.debug("SET_ATTRIBUTES: OUT:" + attributesMapMessage.toString());
        }

        @Override
        public AttributesMap getAttributes(PathName pathName) throws RemoteException {
            try {

                Address address = new Address(ZMIModule.class, ZMIModule.GET_ATTRIBUTES);
                Address responseAddress = new Address(RMIModule.class, RESPOND_ATTRIBUTES);
                Message message = new PathNameRequestMessage(responseAddress, pathName);
                sendMessage(address, message);

                LOGGER.debug("GET_ATTRIBUTES: OUT:" + message.toString());

                return attributesQueue.poll(rmiTimeout, TimeUnit.MILLISECONDS);

            } catch (Exception e) {
                LOGGER.error("GET_ATTRIBUTES: " + e.getMessage(), e);
                throw new RemoteException();
            }
        }

        @Override
        public void installQuery(Attribute attribute, ValueString value) throws RemoteException {
        }

        @Override
        public void uninstallQuery(Attribute attribute) throws RemoteException {
        }

        @Override
        public Set<PathName> getAgentZones() throws RemoteException {

            try {

                Address address = new Address(ZMIModule.class, ZMIModule.GET_ZONES);
                Address responseAddress = new Address(RMIModule.class, RESPOND_ZONES);
                Message message = new RequestMessage(responseAddress);
                sendMessage(address, message);

                LOGGER.debug("GET_AGENT_ZONES: OUT: " + message.toString());

                return zonesQueue.poll(rmiTimeout, TimeUnit.MILLISECONDS);

            } catch (Exception e) {
                LOGGER.error("GET_AGENT_ZONES: OUT: " + e.getMessage(), e);
                throw new RemoteException();
            }
        }

        @Override
        public void setFallbackContacts(Set<ValueContact> contacts) throws RemoteException {
            Address address = new Address(ZMIModule.class, GossipModule.SET_FALLBACK_CONTACTS);
            ContactsMessage contactsMessage = new ContactsMessage(contacts);
            sendMessage(address, contactsMessage);
            LOGGER.debug("SET_FALLBACK_CONTACTS: OUT: " + contactsMessage.toString());
        }
    };

    @Handler(RESPOND_ATTRIBUTES)
    private final MessageHandler<?> h1 = new MessageHandler<AttributesMapMessage>() {
        @Override
        protected void handle(AttributesMapMessage message) {
            try {

                LOGGER.debug("RESPOND_ATTRIBUTES: IN: " + message.toString());

                attributesQueue.put(message.getAttributesMap());

            } catch (Exception e) {
                LOGGER.error("RESPOND_ATTRIBUTES: " + e.getMessage(), e);
            }
        }
    };

    @Handler(RESPOND_ZONES)
    private final MessageHandler<?> h2 = new MessageHandler<ZonesMessage>() {
        @Override
        protected void handle(ZonesMessage message) {
            try {

                LOGGER.debug("RESPOND_ZONES: IN:" + message.toString());

                zonesQueue.put(message.getZones());

            } catch (Exception e) {
                LOGGER.error("RESPOND_ZONES: " + e.getMessage(), e);
            }
        }
    };

    @Override
    public void initialize(ConfigurationProvider configurationProvider) {

        String agentId = configurationProvider.getProperty("Agent.agentId", String.class);
        rmiTimeout = configurationProvider.getProperty("Agent.RMIModule.rmiTimeout", Long.class);

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            AgentInterface stub = (AgentInterface) UnicastRemoteObject.exportObject(server, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(agentId, stub);

        } catch (RemoteException e) {
            LOGGER.error("RMI module error", e);
            throw new IllegalStateException();
        }
    }
}
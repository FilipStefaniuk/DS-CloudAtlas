package pl.edu.mimuw.cloudatlas.agent.modules;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.edu.mimuw.cloudatlas.agent.AgentRMIInterface;
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

@Module(value = "RMIModule", unique = true, dependencies = {"ZMIModule"})
public class RMIModule extends ModuleBase {

    private static Logger LOGGER = LogManager.getLogger(RMIModule.class);

    private static final int RESPOND_ATTRIBUTES = 1;
    private static final int RESPOND_ZONES = 2;

    private static final Long RMI_TIMEOUT = 5000L;

    private BlockingQueue<AttributesMap> attributesQueue = new LinkedBlockingQueue<>();
    private BlockingQueue<Set<PathName>> zonesQueue = new LinkedBlockingQueue<>();

    private AgentRMIInterface server = new AgentRMIInterface() {

        @Override
        public void setAttributes(PathName pathName, AttributesMap attributesMap) throws RemoteException {
            Address address = new Address(ZMIModule.class, ZMIModule.UPDATE_ATTRIBUTES);
            AttributesMapMessage attributesMapMessage = new AttributesMapMessage(attributesMap, pathName);
            sendMessage(address, attributesMapMessage);
        }

        @Override
        public AttributesMap getAttributes(PathName pathName) throws RemoteException {

            Address address = new Address(ZMIModule.class, ZMIModule.GET_ATTRIBUTES);
            Address responseAddress = new Address(RMIModule.class, RESPOND_ATTRIBUTES);
            sendMessage(address, new PathNameRequestMessage(responseAddress, pathName));

            try {

                return attributesQueue.poll(RMI_TIMEOUT, TimeUnit.MILLISECONDS);

            } catch (InterruptedException e) {
                throw new RemoteException();
            }
        }

        @Override
        public void installQuery(Attribute attribute, ValueString value) throws RemoteException {}

        @Override
        public void uninstallQuery(Attribute attribute) throws RemoteException {}

        @Override
        public Set<PathName> getAgentZones() throws RemoteException {

            Address address = new Address(ZMIModule.class, ZMIModule.GET_ZONES);
            Address responseAddress = new Address(RMIModule.class, RESPOND_ZONES);
            sendMessage(address, new RequestMessage(responseAddress));

            try {

                return zonesQueue.poll(RMI_TIMEOUT, TimeUnit.MILLISECONDS);

            } catch (InterruptedException e) {
                throw new RemoteException();
            }
        }

        @Override
        public void setFallbackContacts(Set<ValueContact> contacts) throws RemoteException {
            Address address = new Address(ZMIModule.class, ZMIModule.SET_FALLBACK_CONTACTS);
            ContactsMessage contactsMessage = new ContactsMessage(contacts);
            sendMessage(address, contactsMessage);
        }
    };

    @Handler(RESPOND_ATTRIBUTES)
    private final MessageHandler<?> h1 = new MessageHandler<AttributesMapMessage>() {
        @Override
        protected void handle(AttributesMapMessage message) {

            LOGGER.debug("RESPOND_ATTRIBUTES handler.");

            try {
                attributesQueue.put(message.getAttributesMap());
            } catch (InterruptedException e) {
                LOGGER.error("Failed to respond with Attributes.", e);
            }
        }
    };

    @Handler(RESPOND_ZONES)
    private final MessageHandler<?> h2 = new MessageHandler<ZonesMessage>() {
        @Override
        protected void handle(ZonesMessage message) {

            LOGGER.debug("RESPOND_ZONES handler.");

            try {
                zonesQueue.put(message.getZones());
            } catch (InterruptedException e) {
                LOGGER.error("Failed to respond with Zones.", e);
            }
        }
    };

    public RMIModule() {

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {

            AgentRMIInterface stub = (AgentRMIInterface) UnicastRemoteObject.exportObject(server, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("Agent", stub);

        } catch (RemoteException e) {
            LOGGER.error("RMI module error", e);
            throw new IllegalStateException();
        }
    }
}

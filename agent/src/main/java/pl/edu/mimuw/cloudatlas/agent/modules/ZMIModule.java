package pl.edu.mimuw.cloudatlas.agent.modules;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pl.edu.mimuw.cloudatlas.agent.framework.*;
import pl.edu.mimuw.cloudatlas.agent.messages.*;
import pl.edu.mimuw.cloudatlas.interpreter.InterpreterMain;
import pl.edu.mimuw.cloudatlas.model.*;

import java.util.*;

@Module(value = "ZMIModule", unique = true, dependencies = {"InterpreterModule"})
public class ZMIModule extends ModuleBase {

    private static Logger LOGGER = LogManager.getLogger(ZMIModule.class);

    public static final int UPDATE_ATTRIBUTES = 1;
    public static final int GET_ATTRIBUTES = 2;
    public static final int GET_ZONES = 3;
    public static final int INSTALL_QUERY = 4;
    public static final int UNINSTALL_QUERY = 5;
    public static final int SET_FALLBACK_CONTACTS = 6;

    private ZMI root = InterpreterMain.createDefaultInterpreterHierarchy();
    private Set<ValueContact> fallbackContacts;

    @Handler(UPDATE_ATTRIBUTES)
    private final MessageHandler<?> h1 = new MessageHandler<AttributesMapMessage>() {
        @Override
        protected void handle(AttributesMapMessage message) {

            LOGGER.debug("SET_ATTRIBUTES handler: " + message.getPathName().toString());

            ZMI zmi = message.getPathName().findZMI(root);
            if (zmi != null) {
                for (Map.Entry<Attribute, Value> entry : message.getAttributesMap()) {
                    zmi.getAttributes().addOrChange(entry);
                }

                if (!zmi.equals(root)) {
                    ZMI father = zmi.getFather();
                    PathName pathName = PathName.getPathName(father);
                    Address responseAddress = message.getAddress();
                    Address address = new Address(InterpreterModule.class, InterpreterModule.EXEC_QUERIES);
                    sendMessage(address, new ZMIRequestMessage(responseAddress, pathName, father));
                }
            } else {
                LOGGER.warn("Failed to update ZMI:" + message.getPathName().toString());
            }
        }
    };

    @Handler(GET_ATTRIBUTES)
    private final MessageHandler<?> h2 = new MessageHandler<PathNameRequestMessage>() {
        @Override
        protected void handle(PathNameRequestMessage message) {

            LOGGER.debug("GET_ATTRIBUTES handler.");

            ZMI zmi = message.getPathName().findZMI(root);
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

            LOGGER.debug("GET_ZONES handler.");

            sendMessage(message.getResponseAddress(), new ZonesMessage(getZones(root)));

        }

        private Set<PathName> getZones(ZMI zmi) {
            Set<PathName> pathNames = new HashSet<>();
            pathNames.add(PathName.getPathName(zmi));

            if (!zmi.getSons().isEmpty())
                for(ZMI son : zmi.getSons())
                    pathNames.addAll(getZones(son));

            return pathNames;
        }
    };

    @Handler(SET_FALLBACK_CONTACTS)
    private final MessageHandler<?> h6 = new MessageHandler<ContactsMessage>() {
        @Override
        protected void handle(ContactsMessage message) {

            LOGGER.debug("SET_FALLBACK_CONTACTS handler.");

            fallbackContacts = message.getContacts();
        }

    };


}

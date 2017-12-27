package pl.edu.mimuw.cloudatlas.agent.zmi;

import pl.edu.mimuw.cloudatlas.agent.framework.Message;
import pl.edu.mimuw.cloudatlas.agent.framework.MessageHandler;
import pl.edu.mimuw.cloudatlas.agent.framework.Module;
import pl.edu.mimuw.cloudatlas.agent.interpreter.InterpretQueryMessage;
import pl.edu.mimuw.cloudatlas.agent.interpreter.InterpreterModule;
import pl.edu.mimuw.cloudatlas.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class ZmiModule extends Module {

    public static final Integer UPDATE_ATTRIBUTES = 1;
    public static final Integer INSTALL_QUERY = 2;
    public static final Integer UNINSTALL_QUERY = 3;
    public static final Integer GET_ZONES = 4;
    public static final Integer GET_ATTRIBUTES = 5;
    public static final Integer SET_FALLBACK_CONTACTS = 6;

    private ZMI root;
    private Set<ValueContact> fallbackContacts;

    private Integer interpreterId;

    public ZmiModule(InterpreterModule interpreterModule) {
        super(new HashSet<>(Arrays.asList(interpreterModule)));

        this.interpreterId = interpreterModule.getId();

        registerHandler(UPDATE_ATTRIBUTES, new MessageHandler<AttributesMessage>() {
            @Override
            protected void handle(AttributesMessage message) {
                try {
                    ZMI zmi = getZMIByPath(root, message.getPathName().getComponents());
                    for(Map.Entry<Attribute, Value> entry: message.getAttributesMap()) {
                        zmi.getAttributes().addOrChange(entry);
                    }

                    if (zmi.equals(root)) {
                        ZMI father = zmi.getFather();
                        InterpretQueryMessage msg = new InterpretQueryMessage(zmi.getFather(), message.getPathName().levelUp(), getId(), UPDATE_ATTRIBUTES);
                        sendMessage(interpreterId, InterpreterModule.EXEC_QUERIES, msg);
                    }

                } catch (Exception e) {}
            }
        });

        registerHandler(GET_ATTRIBUTES, new MessageHandler<PathMessage>() {
            @Override
            protected void handle(PathMessage message) {
                try {
                    ZMI zmi = getZMIByPath(root, message.getPathName().getComponents());
                    AttributesMessage resp = new AttributesMessage(zmi.getAttributes(), message.getPathName());
                    respond(message, resp);
                } catch (Exception e) {

                }
            }
        });

        registerHandler(SET_FALLBACK_CONTACTS, new MessageHandler<Message>() {
            @Override
            protected void handle(Message message) {

            }
        });
    }

    private ZMI getZMIByPath(ZMI zmi, List<String> path) throws Exception {

        if (path.isEmpty())
            return zmi;

        for(ZMI son : zmi.getSons()) {
            if (((ValueString) son.getAttributes().get("name")).getValue().equals(path.get(0))) {
                return getZMIByPath(son, path.stream().skip(1).collect(Collectors.toList()));
            }
        }

        throw new Exception();
    }

    private PathName getPathName(ZMI zmi) {
        String name = ((ValueString)zmi.getAttributes().get("name")).getValue();
        return zmi.getFather() == null? PathName.ROOT : getPathName(zmi.getFather()).levelDown(name);
    }
}

package pl.edu.mimuw.cloudatlas.agent.framework;

import org.apache.commons.lang.SerializationUtils;

import java.lang.reflect.Field;
import java.util.*;

abstract public class ModuleBase {

    private String name;
    private Boolean unique;
    private EventQueue eventQueue;
    private List<String> dependencies;
    private Map<Integer, MessageHandler<?>> handlers;

    public static ModuleBase.ModuleFactory moduleFacotry(EventQueue eventQueue) {
        return new ModuleFactory(eventQueue);
    }

    static class ModuleFactory {

        EventQueue eventQueue;

        private ModuleFactory(EventQueue eventQueue) {
            this.eventQueue = eventQueue;
        }

        ModuleBase createModule(Class<? extends ModuleBase> clazz) throws InternalError, IllegalArgumentException {

            Module annotation = clazz.getAnnotation(Module.class);

            if (annotation == null) {
                throw new IllegalArgumentException("Module must be annotated with '@Module'.");
            }

            try {

                ModuleBase object = clazz.newInstance();
                object.name = annotation.value();
                object.unique = annotation.unique();
                object.dependencies = Arrays.asList(annotation.dependencies());
                object.eventQueue = eventQueue;

                object.handlers = new HashMap<>();
                for (Field field : clazz.getDeclaredFields()) {
                    Handler handler = field.getAnnotation(Handler.class);
                    if (handler != null) {

                        if (!field.getType().equals(MessageHandler.class)) {
                            throw new IllegalStateException("Handlers must have type MessageHandler");
                        }

                        if (object.handlers.containsKey(handler.value())) {
                            throw new IllegalStateException("Handler keys must be unique.");
                        }

                        field.setAccessible(true);
                        object.handlers.put(handler.value(), (MessageHandler<?>) field.get(object));
                    }
                }

                return object;

            } catch (InstantiationException | IllegalAccessException e) {
                throw new InternalError("Failed to create module instance: " + annotation.value());
            }
        }
    }

    protected ModuleBase () {}

    String getName() {
        return name;
    }

    Boolean getUnique() {
        return unique;
    }

    List<String> getDependencies() {
        return dependencies;
    }

    void handle(Message msg) {
        MessageHandler<?> handler = handlers.get(msg.getAddress().getHandler());
        handler.handleMessage(msg);
    }

    public void sendMessage(Address address, Message msg) {
            msg.setAddress(address);
            eventQueue.sendMessage((Message) SerializationUtils.clone(msg));
    }

    public void shutdown() {
        eventQueue.shutdown();
    }
}

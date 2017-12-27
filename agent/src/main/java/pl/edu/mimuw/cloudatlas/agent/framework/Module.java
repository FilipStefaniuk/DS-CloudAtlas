package pl.edu.mimuw.cloudatlas.agent.framework;

import com.sun.org.apache.xpath.internal.operations.Mod;

import java.util.*;

abstract public class Module {

    private static Integer nextId = 0;

    private final Integer id;
    private EventQueue eventQueue;
    private Set<Integer> dependencies;
    protected Map<Integer, MessageHandler<?>> handlers;

    public Module() {
        this(new HashSet<>());
    }

    public Module(Set<Module> dependencies) {
        this.id = nextId++;
        this.handlers = new HashMap<>();
        this.dependencies = new HashSet<>();

        for (Module module : dependencies) {
            this.dependencies.addAll(module.dependencies);
            this.dependencies.add(module.id);
        }
    }

    public void handle(Integer type, Message msg) {

        MessageHandler<?> handler = handlers.get(type);
        handler.handleMessage(msg);
    }

    public void setEventQueue(EventQueue eventQueue) {
        this.eventQueue = eventQueue;
    }

    public Integer getId() {
        return id;
    }

    public Set<Integer> getDependencies() {
        return dependencies;
    }

    public void sendMessage(Integer dest, Integer type, Message msg) throws InterruptedException{
        eventQueue.sendMessage(new MessageWrapper<>(id, dest, type, msg));
    }

    public void respond(MessageWithResponse received, Message send) throws InterruptedException{
        sendMessage(received.getSenderID(), received.getHandlerID(), send);
    }

    protected void registerHandler(Integer id, MessageHandler<?> handler) {
        handlers.put(id, handler);
    }
}

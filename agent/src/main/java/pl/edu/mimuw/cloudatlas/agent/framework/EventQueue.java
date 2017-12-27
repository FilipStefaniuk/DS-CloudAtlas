package pl.edu.mimuw.cloudatlas.agent.framework;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class EventQueue {

    private Random rand = new Random();
    private List<Executor> executors = new ArrayList<>();
    private Map<String, List<BlockingQueue<Message>>> queueMap = new HashMap<>();

    static Builder builder() {
        return new EventQueue.Builder();
    }

    static class Builder {

        private EventQueue eventQueue = new EventQueue();
        private Map<String, Class<? extends ModuleBase>> registeredModules = new HashMap<>();
        private Set<String> dependencies = new HashSet<>();
        private ModuleBase.ModuleFactory moduleFactory = ModuleBase.moduleFacotry(eventQueue);

        private Builder() {}

        @SafeVarargs
        public final Builder executor(Class<? extends ModuleBase> ... modules) throws IllegalStateException, IllegalArgumentException {

            BlockingQueue<Message> executorQueue = new LinkedBlockingQueue<>();

            Map<String, ModuleBase> executorModules = new HashMap<>();

            for (Class<? extends ModuleBase> module : Arrays.stream(modules).distinct().collect(Collectors.toList())) {

                ModuleBase object = moduleFactory.createModule(module);

                if (registeredModules.containsKey(object.getName())) {
                    if (!registeredModules.get(object.getName()).equals(module)) {
                        throw new IllegalStateException("Module names must be unique.");
                    } else if (object.getUnique()){
                        throw new IllegalStateException("Multiple instances of unique module.");
                    }
                } else {
                    eventQueue.queueMap.put(object.getName(), new ArrayList<>());
                }

                executorModules.put(object.getName(), object);

                dependencies.addAll(object.getDependencies());
                registeredModules.put(object.getName(), module);
                eventQueue.queueMap.get(object.getName()).add(executorQueue);
            }

            eventQueue.executors.add(new Executor(executorModules, executorQueue));
            return this;
        }

        public EventQueue build() throws IllegalStateException {
            dependencies.removeAll(registeredModules.keySet());
            if (!dependencies.isEmpty()) {
                throw new IllegalStateException("Missing module dependencies: " + dependencies.toString());
            }

            return eventQueue;
        }
    }

    private EventQueue() {}

    void sendMessage(Message msg) throws InterruptedException {
        List<BlockingQueue<Message>> list = queueMap.get(msg.getAddress().getModule());
        list.get(rand.nextInt(list.size())).put(msg);
    }

    void shutdown() throws InterruptedException {
        for(List<BlockingQueue<Message>> queueList : queueMap.values()) {
            for(BlockingQueue<Message> queue : queueList) {
                queue.put(new ShutdownMessage());
            }
        }
    }

    public void start() throws InterruptedException {

        List<Thread> threads = new ArrayList<>();
        for (Executor e : executors) {
            Thread t = new Thread(e);
            threads.add(t);
            t.start();
        }

        for (Thread t: threads) {
            t.join();
        }
    }
}

package pl.edu.mimuw.cloudatlas.agent.framework;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class EventQueue {
    private List<Executor> executors = new ArrayList<>();
    private Map<Integer, BlockingQueue<MessageWrapper<?>>> eventQueue = new HashMap<>();

    public static Builder builder() {
        return new EventQueue.Builder();
    }

    public static class Builder {
        private EventQueue eventQueue = new EventQueue();
        private Set<Module> modules = new HashSet<>();

        private Builder() {}

        public Builder addExecutor(List<Module> modules) {
            this.modules.addAll(modules);
            BlockingQueue<MessageWrapper<?>> queue = new LinkedBlockingQueue<>();
            for (Module module : modules) {
                eventQueue.eventQueue.put(module.getId(), queue);
                module.setEventQueue(eventQueue);
            }
            eventQueue.executors.add(new Executor(modules, queue));
            return this;
        }

        public EventQueue build() throws Exception{
            checkDependencies();
            return eventQueue;
        }

        private void checkDependencies() throws Exception{
            Set<Integer> dependencies = new HashSet<>();
            for (Module module : this.modules) {
                dependencies.addAll(module.getDependencies());
            }

            dependencies.removeAll(this.modules.stream().map(Module::getId).collect(Collectors.toList()));
            if (!dependencies.isEmpty())
                throw new Exception();
        }
    }

    private EventQueue() {}

    public void sendMessage(MessageWrapper<?> m) throws InterruptedException{
        eventQueue.get(m.getDestination()).put(m);
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

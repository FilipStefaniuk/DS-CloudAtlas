package pl.edu.mimuw.cloudatlas.agent.framework;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.provider.ConfigurationProviderBuilder;
import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.classpath.ClasspathConfigurationSource;

import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class EventQueue {

    private Random rand = new Random();
    private UUID id = UUID.randomUUID();
    private List<Executor> executors = new ArrayList<>();
    private Map<String, List<BlockingQueue<Message>>> queueMap = new HashMap<>();

    private static Logger LOGGER = LogManager.getLogger(EventQueue.class);

    public static Builder builder(String configuration) {
        return new EventQueue.Builder(configuration);
    }

    public static class Builder {
        private EventQueue eventQueue = new EventQueue();
        private Map<String, Class<? extends ModuleBase>> registeredModules = new HashMap<>();
        private Set<String> dependencies = new HashSet<>();
        private ModuleBase.ModuleFactory moduleFactory = ModuleBase.moduleFacotry(eventQueue);
        private ConfigurationProvider config;

        private Builder(String configuration) {

            ConfigurationSource source = new ClasspathConfigurationSource(() -> Paths.get(configuration));

            config = new ConfigurationProviderBuilder()
                    .withConfigurationSource(source)
                    .build();
        }

        @SafeVarargs
        public final Builder executor(Class<? extends ModuleBase> ... modules) throws IllegalStateException, IllegalArgumentException {

            BlockingQueue<Message> executorQueue = new LinkedBlockingQueue<>();

            Map<String, ModuleBase> executorModules = new HashMap<>();

            for (Class<? extends ModuleBase> module : Arrays.stream(modules).distinct().collect(Collectors.toList())) {

                ModuleBase object = moduleFactory.createModule(module, config);

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

    // If message is not addressed - broadcast.
    public void sendMessage(Message msg) {

        try {

            List<BlockingQueue<Message>> list = queueMap.get(msg.getAddress().getModule());
            list.get(rand.nextInt(list.size())).put(msg);

        } catch (InterruptedException e) {
            LOGGER.error("Failed to send message.", e);
        }
    }

    // Change shutdown to broadcast shutdown message.
    public void shutdown() {

        LOGGER.info("Shutting down event queue.");

        try {
            for (List<BlockingQueue<Message>> queueList : queueMap.values()) {
                for (BlockingQueue<Message> queue : queueList) {
                    queue.put(new ShutdownMessage());
                }
            }
        } catch (InterruptedException e) {
            LOGGER.error("Failed to shut down event queue.", e);
        }
    }

    public void start() {

        LOGGER.info("Starting event queue.");

        List<Thread> threads = new ArrayList<>();
        for (Executor e : executors) {
            Thread t = new Thread(e);
            threads.add(t);
            t.start();
        }

//        try {
//            for (Thread t : threads) {
//                t.join();
//            }
//        } catch (InterruptedException e) {
//            LOGGER.error("Message queue interrupted.", e);
//        }
    }

    public UUID getId() {
        return id;
    }
}

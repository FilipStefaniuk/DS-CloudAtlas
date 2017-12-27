package pl.edu.mimuw.cloudatlas.agent.framework;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class Executor implements Runnable {


    private Map<Integer, Module> modules;
    private BlockingQueue<MessageWrapper<?>> messageQueue;

    Executor(List<Module> modules, BlockingQueue<MessageWrapper<?>> queue) {
        this.messageQueue = queue;
        this.modules = new HashMap<>();
        for (Module module : modules) {
            this.modules.put(module.getId(), module);
        }
    }

    @Override
    public void run() {
        boolean stop = false;
        while (!stop) {
            try {

                MessageWrapper<?> msg = messageQueue.take();
                modules.get(msg.getDestination()).handle(msg.getType(), msg.getMessage());

            } catch (Exception e) {}
        }
    }
}

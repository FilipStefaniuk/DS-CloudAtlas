package pl.edu.mimuw.cloudatlas.agent.framework;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class Executor implements Runnable {

    private Map<String, ModuleBase> modules;
    private BlockingQueue<Message> messageQueue;

    Executor(Map<String, ModuleBase> modules, BlockingQueue<Message> queue) {
        this.modules = modules;
        this.messageQueue = queue;
    }

    @Override
    public void run() {
        boolean stop = false;
        while (!stop) {
            try {
                Message msg = messageQueue.take();
                if (msg.getClass() == ShutdownMessage.class) {
                    stop = true;
                } else {
                    modules.get(msg.getAddress().getModule()).handle(msg);
                }
            } catch (InterruptedException e) {}
        }

//        // Add if problems with shutdown
//        for (ModuleBase moduleBase : modules.values()) {
//            moduleBase.shutdown();
//        }
    }
}

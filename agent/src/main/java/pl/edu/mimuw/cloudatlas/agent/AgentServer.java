package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.agent.framework.EventQueue;
import pl.edu.mimuw.cloudatlas.agent.modules.InterpreterModule;
import pl.edu.mimuw.cloudatlas.agent.modules.RMIModule;
import pl.edu.mimuw.cloudatlas.agent.modules.ZMIModule;

public class AgentServer {

    public static void main(String[] args) throws Exception {
        EventQueue eq = EventQueue.builder()
                .executor(RMIModule.class)
                .executor(ZMIModule.class)
                .executor(InterpreterModule.class)
                .executor(InterpreterModule.class)
                .build();

        eq.start();
    }
}

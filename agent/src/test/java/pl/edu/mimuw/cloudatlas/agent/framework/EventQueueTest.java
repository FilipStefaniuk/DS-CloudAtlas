package pl.edu.mimuw.cloudatlas.agent.framework;

import org.junit.Test;


public class EventQueueTest {


    static class NotAnnotated extends ModuleBase {}

    @Module(name = "Module1")
    static class Module1 extends ModuleBase {

        @Handler(id = 1)
        MessageHandler<?> handler1 = new MessageHandler<Message>() {
            @Override
            protected void handle(Message message) {

            }
        };
    }

    @Module(name = "Module2", dependencies = {"Module1"})
    static class Module2 extends ModuleBase {}

    @Module(name = "Module3", unique = true)
    static class Module3 extends ModuleBase {}

    @Module(name = "Module1")
    static class Module4 extends ModuleBase {}

    @Module(name = "Module5")
    static class Module5 extends ModuleBase {
        @Handler(id = 1)
        MessageHandler<?> handler1 = new MessageHandler<Message>() {
            @Override
            protected void handle(Message message) {

            }
        };

        @Handler(id = 1)
        MessageHandler<?> handler2 = new MessageHandler<Message>() {
            @Override
            protected void handle(Message message) {

            }
        };
    }

    @Module(name = "Module6")
    static class Module6 extends ModuleBase {

        @Handler(id = 1)
        Integer field;
    }

//  ----------------------------------------------------------------------------------------


    @Test
    public void simpleEventQueueTest() throws Exception {
        EventQueue.builder()
                .executor(Module1.class, Module2.class)
                .executor(Module1.class, Module1.class)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void noAnnotationTest() throws Exception {
        EventQueue.builder()
                .executor(NotAnnotated.class)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void notUniqueTest() throws Exception {
        EventQueue.builder()
                .executor(Module3.class)
                .executor(Module3.class)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void missingDependencyTest() throws Exception {
        EventQueue.builder()
                .executor(Module2.class)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void duplicateNamesTest() throws Exception {
        EventQueue.builder()
                .executor(Module4.class, Module1.class);
    }

    @Test(expected = IllegalStateException.class)
    public void duplicateHandlerKeys() throws Exception {
        EventQueue.builder()
                .executor(Module5.class)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void wrongHandlerType() throws Exception {
        EventQueue.builder()
                .executor(Module6.class)
                .build();
    }
}

//package pl.edu.mimuw.cloudatlas.agent.framework;
//
//import org.junit.Test;
//
//public class FrameworkTest {
//
//    @Module("Greeter")
//    public static class Greeter extends ModuleBase {
//
//        private static final int HANDLE_ID = 1;
//        private static final Integer MAX = 5;
//
//        private Integer counter = 0;
//
//        @Handler(HANDLE_ID)
//        MessageHandler<?> handler1 = new MessageHandler<Message>() {
//
//            @Override
//            protected void handle(Message message) {
//                System.out.println(Thread.currentThread().getId() + ": Hello!");
//                counter++;
//                if (counter > MAX)
//                    shutdown();
//                else{
//                    sendMessage(new Address(Greeter.class, Greeter.HANDLE_ID), new GenericMessage());
//                }
//            }
//        };
//    }
//
//    private static class GenericMessage extends Message {}
//
////  --------------------------------------------------------------------------------------------------------------------
//
//    @Test
//    public void simpleTest() throws Exception{
//        EventQueue eventQueue = EventQueue.builder()
//                .executor(Greeter.class)
//                .executor(Greeter.class)
//                .executor(Greeter.class)
//                .build();
//
//        GenericMessage message = new GenericMessage();
//        message.setAddress(new Address(Greeter.class, Greeter.HANDLE_ID));
//        eventQueue.sendMessage(message);
//        eventQueue.start();
//    }
//}

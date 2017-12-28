//package pl.edu.mimuw.cloudatlas.agent.modules;
//
//import pl.edu.mimuw.cloudatlas.agent.framework.*;
//import pl.edu.mimuw.cloudatlas.agent.messages.IdMessage;
//import pl.edu.mimuw.cloudatlas.agent.messages.TimerMessage;
//
//import java.util.Calendar;
//import java.util.concurrent.PriorityBlockingQueue;
//
//@Module(value = "TimerModule", unique = true)
//public class TimerModule extends ModuleBase implements Runnable {
//
//    private static final class ScheduledItem implements Comparable<ScheduledItem> {
//        private Integer id;
//        private Address address;
//        private Long time;
//
//        ScheduledItem(TimerMessage message) {
//            this.id = message.getId();
//            this.address = message.getAddress();
//            this.time = message.getStartTime() + message.getDelay();
//        }
//
//        @Override
//        public int compareTo(ScheduledItem scheduledItem) {
//            if (this.time.equals(scheduledItem.time))
//                return 0;
//            return this.time > scheduledItem.time ? 1 : -1;
//        }
//
//        @Override
//        public boolean equals(Object o) {
//            if (this == o) return true;
//            if (o == null || getClass() != o.getClass()) return false;
//
//            ScheduledItem that = (ScheduledItem) o;
//
//            if (!id.equals(that.id)) return false;
//            return address.equals(that.address);
//        }
//
//        @Override
//        public int hashCode() {
//            int result = id.hashCode();
//            result = 31 * result + address.hashCode();
//            return result;
//        }
//    }
//
//    public static final int SCHEDULE = 1;
//    public static final int CANCEL = 2;
//
//    private Thread scheduler = new Thread(this);
//    private PriorityBlockingQueue<ScheduledItem> scheduleQueue = new PriorityBlockingQueue<>();
//
//    @Handler(SCHEDULE)
//    private final MessageHandler<?> h1 = new MessageHandler<TimerMessage>() {
//        @Override
//        protected void handle(TimerMessage message) {
//            scheduleQueue.add(new ScheduledItem(message));
//            scheduler.notify();
//        }
//    };
//
//    @Handler(CANCEL)
//    private final MessageHandler<?> h2 = new MessageHandler<TimerMessage>() {
//        @Override
//        protected void handle(TimerMessage message) {
//            scheduleQueue.remove(new ScheduledItem(message));
//        }
//    };
//
//    @Override
//    public void run() {
//        while (true) {
//            try {
//                if (scheduleQueue.isEmpty())
//                    wait();
//                else {
//                    ScheduledItem item = scheduleQueue.take();
//                    Long diff = item.time - Calendar.getInstance().get(Calendar.MILLISECOND);
//                    if (diff < 0) {
//                        sendMessage(item.address, new IdMessage(item.id));
//                    } else {
//                        scheduleQueue.add(item);
//                        wait(diff);
//                    }
//                }
//            } catch (InterruptedException e) {}
//        }
//    }
//}

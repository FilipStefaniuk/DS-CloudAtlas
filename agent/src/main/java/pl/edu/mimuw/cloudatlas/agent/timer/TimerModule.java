//package pl.edu.mimuw.cloudatlas.agent.timer;
//
//import pl.edu.mimuw.cloudatlas.agent.framework.MessageHandler;
//import pl.edu.mimuw.cloudatlas.agent.framework.ModuleBase;
//
//import java.util.Calendar;
//import java.util.concurrent.PriorityBlockingQueue;
//
//public class TimerModule extends ModuleBase implements Runnable {
//
//    public static final Integer SCHEDULE = 1;
//    public static final Integer CANCEL = 2;
//
//    private Thread scheduler = new Thread(this);
//    private PriorityBlockingQueue<ScheduledItem> scheduleQueue = new PriorityBlockingQueue<>();
//
//    private static final class ScheduledItem implements Comparable<ScheduledItem> {
//        private Integer requestId;
//        private Integer senderId;
//        private Integer handlerId;
//        private Long time;
//
//        ScheduledItem(CancelAlarmMessage message) {
//            this.requestId = message.getRequestID();
//            this.senderId = message.getSenderID();
//        }
//
//        ScheduledItem(ScheduleAlarmMessage message) {
//            this.requestId = message.getRequestID();
//            this.senderId = message.getSenderID();
//            this.handlerId = message.getSenderHandleID();
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
//            if (!requestId.equals(that.requestId)) return false;
//            return senderId.equals(that.senderId);
//        }
//
//        @Override
//        public int hashCode() {
//            int result = requestId.hashCode();
//            result = 31 * result + senderId.hashCode();
//            return result;
//        }
//    }
//
//    public TimerModule() {
//
//        registerHandler(SCHEDULE, new MessageHandler<ScheduleAlarmMessage>() {
//            @Override
//            protected void handle(ScheduleAlarmMessage message) {
//                ScheduledItem item = new ScheduledItem(message);
//                scheduleQueue.add(item);
//                scheduler.notify();
//            }
//        });
//
//        registerHandler(CANCEL, new MessageHandler<CancelAlarmMessage>() {
//            @Override
//            protected void handle(CancelAlarmMessage message) {
//                ScheduledItem item = new ScheduledItem(message);
//                scheduleQueue.remove(item);
//            }
//        });
//    }
//
//    @Override
//    public void run() {
//        try {
//            while (true) {
//                if (scheduleQueue.isEmpty())
//                    wait();
//                ScheduledItem item = scheduleQueue.take();
//                Long diff = item.time - Calendar.getInstance().get(Calendar.MILLISECOND);
//                if (diff < 0) {
//                    sendMessage(item.senderId, item.handlerId, new AlarmMessage(item.requestId));
//                } else {
//                    scheduleQueue.add(item);
//                    wait(diff);
//                }
//            }
//        } catch (InterruptedException e) {}
//    }
//}

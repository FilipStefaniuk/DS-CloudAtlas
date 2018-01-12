package pl.edu.mimuw.cloudatlas.agent.modules;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cfg4j.provider.ConfigurationProvider;
import pl.edu.mimuw.cloudatlas.agent.framework.*;
import pl.edu.mimuw.cloudatlas.agent.messages.TimerMessage;

import java.util.concurrent.PriorityBlockingQueue;

@Module(value = "TimerModule", unique = true)
public class TimerModule extends ModuleBase {

    private final static Logger LOGGER = LogManager.getLogger(TimerModule.class);

    public static final int SCHEDULE = 1;
    public static final int CANCEL = 2;

    private PriorityBlockingQueue<ScheduledMessage> scheduleQueue = new PriorityBlockingQueue<>();

    private Thread scheduler = new Thread(new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    ScheduledMessage item = scheduleQueue.take();
                    Long diff = item.time - System.currentTimeMillis();
                    if (diff < 0) {
                        sendMessage(item.message.getAddress(), item.message);
                    } else {
                        scheduleQueue.add(item);
                    }

                } catch (Exception e) {
                    LOGGER.error("SCHEDULER: " + e.getMessage(), e);
                }
            }
        }

    });

    @Handler(SCHEDULE)
    private final MessageHandler<?> h1 = new MessageHandler<TimerMessage>() {
        @Override
        protected void handle(TimerMessage message) {
            try {

                LOGGER.debug("SCHEDULE: IN: " + message.toString());

                scheduleQueue.add(new ScheduledMessage(message));
            } catch (Exception e) {
                LOGGER.error("SCHEDULE: " + e.getMessage(), e);
            }
        }
    };

    @Handler(CANCEL)
    private final MessageHandler<?> h2 = new MessageHandler<TimerMessage>() {
        @Override
        protected void handle(TimerMessage message) {
            try {

                LOGGER.debug("CANCEL: IN: " + message.toString());

                scheduleQueue.remove(new ScheduledMessage(message));
            } catch (Exception e) {
                LOGGER.error("CANCEL: " + e.getMessage(), e);
            }
        }
    };


    @Override
    public void initialize(ConfigurationProvider configurationProvider) {
        scheduler.start();
    }


    /*********************************************************************************************
                                            Subclasses
     ********************************************************************************************/

    private static final class ScheduledMessage implements Comparable<ScheduledMessage> {

        private Long id;
        private String author;
        private Message message;
        private Long time;

        ScheduledMessage(TimerMessage message) {
            this.id = message.getId();
            this.author = message.getAuthor();
            this.message = message.getMessage();

            if (message.getStartTime() != null && message.getDelay() != null) {
                this.time = message.getStartTime() + message.getDelay();
            }
        }

        @Override
        public int compareTo(ScheduledMessage scheduledMessage) {
            if (this.time.equals(scheduledMessage.time))
                return 0;
            return this.time > scheduledMessage.time ? 1 : -1;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ScheduledMessage that = (ScheduledMessage) o;

            if (id != null ? !id.equals(that.id) : that.id != null) return false;
            return author != null ? author.equals(that.author) : that.author == null;
        }

        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (author != null ? author.hashCode() : 0);
            return result;
        }
    }


}

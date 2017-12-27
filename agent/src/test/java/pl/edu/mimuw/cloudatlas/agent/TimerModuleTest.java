package pl.edu.mimuw.cloudatlas.agent;

import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import org.junit.Test;
import org.powermock.reflect.Whitebox;
import pl.edu.mimuw.cloudatlas.agent.framework.EventQueue;
import pl.edu.mimuw.cloudatlas.agent.timer.ScheduleAlarmMessage;
import pl.edu.mimuw.cloudatlas.agent.timer.TimerModule;

import java.sql.Time;
import java.util.Arrays;
import java.util.concurrent.PriorityBlockingQueue;


//public class TimerModuleTest {
//
//    private static final Integer SENDER_ID = 0;
//    private static final Integer SENDER_HANDLE = 0;
//    private static final Integer REQUEST_ID = 0;
//    private static final Long DELAY = 100L;
//    private static final Long START_TIMER = 1000L;
//
//    @Test
//    public void test1() {
//        TimerModule module = new TimerModule();
//        ScheduleAlarmMessage msg = new ScheduleAlarmMessage(SENDER_ID, SENDER_HANDLE, REQUEST_ID, DELAY, START_TIMER);
//        module.handle(TimerModule.SCHEDULE_TYPE, msg);
//        PriorityBlockingQueue<?> queue = Whitebox.getInternalState(module, "scheduleQueue");
//        System.out.println(queue);
//    }
//
//
//}

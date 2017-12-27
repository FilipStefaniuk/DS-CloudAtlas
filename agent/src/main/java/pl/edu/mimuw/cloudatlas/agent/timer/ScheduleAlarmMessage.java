package pl.edu.mimuw.cloudatlas.agent.timer;

import pl.edu.mimuw.cloudatlas.agent.framework.Message;

import java.time.Duration;

public class ScheduleAlarmMessage extends Message {

    private Integer senderID;
    private Integer senderHandleID;
    private Integer requestID;
    private Long delay;
    private Long startTime;

    public ScheduleAlarmMessage(Integer senderID, Integer senderHandleID, Integer requestID, Long delay, Long startTime) {
        this.senderID = senderID;
        this.senderHandleID = senderHandleID;
        this.requestID = requestID;
        this.delay = delay;
        this.startTime = startTime;
    }

    public Integer getSenderID() {
        return senderID;
    }

    public Integer getSenderHandleID() {
        return senderHandleID;
    }

    public Integer getRequestID() {
        return requestID;
    }

    public Long getDelay() {
        return delay;
    }

    public Long getStartTime() {
        return startTime;
    }
}

package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.agent.framework.Address;
import pl.edu.mimuw.cloudatlas.agent.framework.RequestMessage;

public class TimerMessage extends RequestMessage {
    private Integer id;
    private Long startTime;
    private Long delay;

    public TimerMessage(Address responseAddress, Integer id, Long startTime, Long delay) {
        super(responseAddress);
        this.id = id;
        this.startTime = startTime;
        this.delay = delay;
    }

    public Integer getId() {
        return id;
    }

    public Long getStartTime() {
        return startTime;
    }

    public Long getDelay() {
        return delay;
    }
}

package pl.edu.mimuw.cloudatlas.agent.timer;

import pl.edu.mimuw.cloudatlas.agent.framework.Message;

public class AlarmMessage extends Message {
    private Integer requestID;

    public AlarmMessage(Integer requestID) {
        this.requestID = requestID;
    }

    public Integer getRequestID() {
        return requestID;
    }
}

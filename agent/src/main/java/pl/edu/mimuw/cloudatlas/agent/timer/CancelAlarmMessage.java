package pl.edu.mimuw.cloudatlas.agent.timer;

import pl.edu.mimuw.cloudatlas.agent.framework.Message;

public class CancelAlarmMessage extends Message {
    private Integer senderID;
    private Integer requestID;

    public CancelAlarmMessage(Integer senderID, Integer requestID) {
        this.senderID = senderID;
        this.requestID = requestID;
    }

    public Integer getSenderID() {
        return senderID;
    }

    public Integer getRequestID() {
        return requestID;
    }
}

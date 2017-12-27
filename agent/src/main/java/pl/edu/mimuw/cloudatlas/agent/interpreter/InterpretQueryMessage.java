package pl.edu.mimuw.cloudatlas.agent.interpreter;

import pl.edu.mimuw.cloudatlas.agent.framework.Message;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ZMI;

public class InterpretQueryMessage extends Message {

    private ZMI zmi;
    private PathName pathName;
    private Integer senderId;
    private Integer senderHandleId;

    public InterpretQueryMessage(ZMI zmi, PathName pathName, Integer senderId, Integer senderHandleId) {
        this.zmi = zmi;
        this.pathName = pathName;
        this.senderId = senderId;
        this.senderHandleId = senderHandleId;
    }

    public ZMI getZmi() {
        return zmi;
    }

    public PathName getPath() {
        return pathName;
    }

    public Integer getSenderId() {
        return senderId;
    }

    public Integer getSenderHandleId() {
        return senderHandleId;
    }
}

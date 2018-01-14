package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.agent.framework.GenericMessage;
import pl.edu.mimuw.cloudatlas.model.PathName;

import java.io.Serializable;
import java.net.InetAddress;

public class FromNetworkMessage<T extends Serializable> extends GenericMessage<T> {
    private Long timestampSnd;
    private Long timestampRcv;
    private InetAddress senderAddress;
    private Integer senderPort;
    private PathName senderId;


    public FromNetworkMessage(T value, InetAddress senderAddress, Integer senderPort, PathName senderId) {
        super(value);
        this.senderAddress = senderAddress;
        this.senderPort = senderPort;
        this.senderId = senderId;
    }

    public void setTimestampSnd(Long timestampSnd) {
        this.timestampSnd = timestampSnd;
    }

    public void setTimestampRcv(Long timestampRcv) {
        this.timestampRcv = timestampRcv;
    }

    public Long getTimestampSnd() {
        return timestampSnd;
    }

    public Long getTimestampRcv() {
        return timestampRcv;
    }

    public InetAddress getSenderAddress() {
        return senderAddress;
    }

    public Integer getSenderPort() {
        return senderPort;
    }

    public PathName getSenderId() {
        return senderId;
    }

    @Override
    public String toString() {
        return "FromNetworkMessage{" +
                "timestampSnd=" + timestampSnd +
                ", timestampRcv=" + timestampRcv +
                ", senderAddress=" + senderAddress +
                ", senderPort=" + senderPort +
                ", senderId=" + senderId +
                '}';
    }
}

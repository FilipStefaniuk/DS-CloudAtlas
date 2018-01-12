package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.agent.framework.Message;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.PathName;

import java.net.InetAddress;
import java.util.List;

public class GossipMessage extends Message {
    private List<PathName> requests;
    private List<AttributesMap> data;
    private InetAddress sender;
    private Integer senderPort;

    public GossipMessage(List<PathName> requests, List<AttributesMap> data, InetAddress sender, Integer senderPort) {
        this.requests = requests;
        this.data = data;
        this.sender = sender;
        this.senderPort = senderPort;
    }

    public List<PathName> getRequests() {
        return requests;
    }

    public List<AttributesMap> getData() {
        return data;
    }

    public InetAddress getSender() {
        return sender;
    }

    public Integer getSenderPort() {
        return senderPort;
    }

    @Override
    public String toString() {
        return "GossipMessage{" +
                "requests=" + requests +
                ", data=" + data +
                ", sender=" + sender +
                ", senderPort=" + senderPort +
                '}';
    }
}

package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.agent.framework.Message;
import pl.edu.mimuw.cloudatlas.agent.modules.GossipModule;
import pl.edu.mimuw.cloudatlas.model.PathName;

import java.net.InetAddress;

public class GossipFreshnessMessage extends Message {
    private PathName selectedZone;
    private GossipModule.ZMIInfo info;
    private PathName senderId;
    private InetAddress sender;
    private Integer senderPort;

    public GossipFreshnessMessage(PathName selectedZone, GossipModule.ZMIInfo info, PathName senderId, InetAddress sender, Integer senderPort) {
        this.selectedZone = selectedZone;
        this.info = info;
        this.senderId = senderId;
        this.sender = sender;
        this.senderPort = senderPort;
    }

    public PathName getSelectedZone() {
        return selectedZone;
    }

    public GossipModule.ZMIInfo getInfo() {
        return info;
    }

    public PathName getSenderId() {
        return senderId;
    }

    public InetAddress getSender() {
        return sender;
    }

    public Integer getSenderPort() {
        return senderPort;
    }

    @Override
    public String toString() {
        return "GossipFreshnessMessage{" +
                "selectedZone=" + selectedZone +
                ", info=" + info +
                ", senderId=" + senderId +
                ", sender=" + sender +
                ", senderPort=" + senderPort +
                '}';
    }
}

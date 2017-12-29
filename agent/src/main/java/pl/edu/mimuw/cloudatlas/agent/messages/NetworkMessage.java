package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.agent.framework.Message;

import java.net.InetAddress;

public class NetworkMessage extends Message {
    private InetAddress target;
    private int port;
    private Message message;

    public NetworkMessage(InetAddress target, int port, Message message) {
        this.target = target;
        this.port = port;
        this.message = message;
    }

    public InetAddress getTarget() {
        return target;
    }

    public int getPort() {
        return port;
    }

    public Message getMessage() {
        return message;
    }
}

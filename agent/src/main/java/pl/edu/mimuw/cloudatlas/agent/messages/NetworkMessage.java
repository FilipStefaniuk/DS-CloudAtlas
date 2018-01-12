package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.agent.framework.GenericMessage;
import pl.edu.mimuw.cloudatlas.agent.framework.Message;

import java.io.Serializable;
import java.net.InetAddress;

public class NetworkMessage<T extends Serializable> extends Message {
    private InetAddress target;
    private int port;
    private GenericMessage<T> message;

    public NetworkMessage(InetAddress target, int port, GenericMessage<T> message) {
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

    public GenericMessage<T> getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "NetworkMessage{" +
                "target=" + target +
                ", port=" + port +
                ", message=" + message +
                '}';
    }
}

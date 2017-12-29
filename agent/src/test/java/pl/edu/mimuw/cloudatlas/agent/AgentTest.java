package pl.edu.mimuw.cloudatlas.agent;

import org.apache.commons.lang.SerializationUtils;
import org.junit.Test;
import pl.edu.mimuw.cloudatlas.agent.framework.*;
import pl.edu.mimuw.cloudatlas.agent.messages.IdMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.NetworkMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.PathNameRequestMessage;
import pl.edu.mimuw.cloudatlas.agent.modules.CommunicationModule;
import pl.edu.mimuw.cloudatlas.agent.modules.InterpreterModule;
import pl.edu.mimuw.cloudatlas.agent.modules.RMIModule;
import pl.edu.mimuw.cloudatlas.agent.modules.ZMIModule;

import java.net.InetAddress;
import java.nio.ByteBuffer;

public class AgentTest {

    @Test
    public void testZmiInterpreter() throws Exception {
        EventQueue eq = EventQueue.builder()
                .executor(CommunicationModule.class)
                .build();

        Message msg = new ShutdownMessage();
        msg.setAddress(new Address(CommunicationModule.class, CommunicationModule.SEND));
        NetworkMessage networkMessage = new NetworkMessage(InetAddress.getLocalHost(), 19911, msg);
        networkMessage.setAddress(new Address(CommunicationModule.class, CommunicationModule.SEND));
        eq.sendMessage(networkMessage);
        eq.start();
        new CommunicationModule();
    }

    @Test
    public void testBuffer() throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        buffer.putInt(11);
        buffer.flip();
        int a = buffer.getInt();

        System.out.println(a);
    }
}

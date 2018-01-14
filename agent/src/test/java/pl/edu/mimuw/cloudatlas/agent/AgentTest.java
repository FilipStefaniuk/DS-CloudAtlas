package pl.edu.mimuw.cloudatlas.agent;

import org.junit.Test;
import pl.edu.mimuw.cloudatlas.agent.framework.*;
import pl.edu.mimuw.cloudatlas.agent.messages.*;
import pl.edu.mimuw.cloudatlas.agent.modules.*;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ValueString;
import pl.edu.mimuw.cloudatlas.model.ZMI;

import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class AgentTest {

    @Test
    public void testZmiInterpreter() throws Exception {
//        EventQueue eq = EventQueue.builder()
//                .executor(CommunicationModule.class)
//                .build();
//
//        Message msg = new ShutdownMessage();
//        msg.setAddress(new Address(CommunicationModule.class, CommunicationModule.SEND));
//        NetworkMessage networkMessage = new NetworkMessage(InetAddress.getLocalHost(), 19911, msg);
//        networkMessage.setAddress(new Address(CommunicationModule.class, CommunicationModule.SEND));
//        eq.sendMessage(networkMessage);
//        eq.start();
//        new CommunicationModule();

//        EventQueue eq = EventQueue.builder()
//                .executor(TimerModule.class)
//                .executor(GossipModule.class)
//                .executor(CommunicationModule.class)
//                .executor(ZMIModule.class, InterpreterModule.class)
//                .build();
//
//        Message message = new EmptyMessage();
//        message.setAddress(new Address(GossipModule.class, GossipModule.INIT_GOSSIP));
//        eq.sendMessage(message);
//
//        Message message2 = new AttributesMapMessage(new AttributesMap(), PathName.ROOT);
//        message2.setAddress(new Address(ZMIModule.class, ZMIModule.UPDATE_ATTRIBUTES));
//        eq.sendMessage(message2);
//
//        eq.start();
    }

    @Test
    public void testBuffer() throws Exception {
        PathName pathName1 = new PathName("/uw/cpu01");
        PathName pathName2 = new PathName("/pjwstk/cpu01");

        System.out.println(pathName1.equals(pathName2));
    }

    @Test
    public void testPathName() throws Exception {
//        PathName pathName = new PathName("/uw/violet02");
//        System.out.println(pathName.getComponents());

//        System.out.println(PathName.getLCA(new PathName("/a/v/fdasfasd/c/asdf/sdfs"), new PathName("/a/v/c")));

        ZMI root = new ZMI();
        root.getAttributes().add(ZMIModule.ID, new ValueString(""));

        ZMI zmi1 = new ZMI(root);
        zmi1.getAttributes().add(ZMIModule.ID, new ValueString("/uw"));

        ZMI zmi2 = new ZMI(root);
        zmi2.getAttributes().add(ZMIModule.ID, new ValueString("/pjstk"));

        ZMI zmi3 = new ZMI(zmi1);
        zmi3.getAttributes().add(ZMIModule.ID, new ValueString("/uw/violet07"));

        ZMI zmi4 = new ZMI(zmi1);
        zmi4.getAttributes().add(ZMIModule.ID, new ValueString("/uw/khaki13"));

        root.addSon(zmi1);
        root.addSon(zmi2);

        zmi1.addSon(zmi3);
        zmi1.addSon(zmi4);

        System.out.println(ZMIModule.zmiByID(root, new PathName("/asf")));
    }
}

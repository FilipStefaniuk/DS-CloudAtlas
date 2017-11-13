package pl.edu.mimuw.cloudatlas.agent;

//import pl.edu.mimuw.cloudatlas.model.PathName;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class AgentServer {
    public static void main(String[] args) throws Exception{

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            Agent object = new Agent();
            AgentInterface stub = (AgentInterface) UnicastRemoteObject.exportObject(object, 0);
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind("Agent", stub);
            System.out.println("Agent bound");
        } catch (Exception e) {
            System.err.println("Agent Exception");
            e.printStackTrace();
        }

//        while(true) {
////            System.out.println(agent.getAttributesOfZone(new PathName("/uw/violet07")));
//            System.out.println(agent.getValue());
//            TimeUnit.SECONDS.sleep(3);
//        }
    }
}

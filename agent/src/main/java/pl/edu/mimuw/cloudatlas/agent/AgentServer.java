package pl.edu.mimuw.cloudatlas.agent;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class AgentServer {

    private static final int REGISTRY_PORT = 1324;

    public static void main(String[] args) throws Exception{

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }

        try {
            Agent object = new Agent();
            AgentInterface stub = (AgentInterface) UnicastRemoteObject.exportObject(object, 0);
            Registry registry = LocateRegistry.getRegistry(REGISTRY_PORT);
            registry.rebind("Agent", stub);
//            System.out.println("Agent bound");

            while(true) {
                object.update();
            }

        } catch (Exception e) {
            System.err.println("Agent Exception");
            e.printStackTrace();
        }

    }
}

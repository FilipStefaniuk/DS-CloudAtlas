package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ValueString;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.TimeUnit;

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


//            object.installQuery(new Attribute("&query"), new ValueString("SELECT 2 + 2 AS sum_2"));
//            new Thread(object).start();
//            System.out.println("Agent started running");

            while(true) {
                try {
                    object.executeQueries();
//                    System.out.println(object.getAttributes(new PathName("/uw")));
                } catch(Exception e) {
                    System.out.println("EXCEPTION");
                }

//                TimeUnit.SECONDS.sleep(3);
            }

        } catch (Exception e) {
            System.err.println("Agent Exception");
            e.printStackTrace();
        }

    }
}

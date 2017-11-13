package pl.edu.mimuw.cloudatlas.client;

import pl.edu.mimuw.cloudatlas.agent.AgentInterface;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ValueString;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Client {
    private static final String HOST = "localhost";
    private static final String LOOKUP_NAME = "Agent";

    public static void main(String[] args) throws Exception {
        Registry registry = LocateRegistry.getRegistry(HOST);
        AgentInterface stub = (AgentInterface) registry.lookup(LOOKUP_NAME);
//        stub.uninstallQuery(new Attribute("&query"), new ValueString("SELECT 2 + 2 AS sum_2"));
        System.out.println(stub.getAgentZones());
//        if(args.length > 1)
//            stub.installQuery(new Attribute(args[1]), new ValueString(args[2]));
//        AttributesMap attributes = stub.getAttributes(new PathName(args[0]));
//        System.out.println(attributes);
    }
}

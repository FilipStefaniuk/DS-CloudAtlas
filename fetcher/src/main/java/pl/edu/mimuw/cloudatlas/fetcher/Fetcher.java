package pl.edu.mimuw.cloudatlas.fetcher;

import pl.edu.mimuw.cloudatlas.agent.AgentRMIInterface;
import pl.edu.mimuw.cloudatlas.model.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.TimeUnit;

public class Fetcher {

    private static final String HOST = "localhost";
//    private static final Integer PORT = 1324;
    private static final String ZMI_NAME = "/uw/violet07";
    private static final String LOOKUP_NAME = "Agent";
    private static final Integer INTERVAL = 1;

    public static void main(String[] args) throws Exception {

        SystemInfo systemInfo = new SystemInfo();
        PathName pathName = new PathName(ZMI_NAME);
        Registry registry = LocateRegistry.getRegistry(HOST);
        AgentRMIInterface stub = (AgentRMIInterface) registry.lookup(LOOKUP_NAME);

        while (true) {
            systemInfo.updateAttributes();

            stub.setAttributes(pathName, systemInfo.getAttributes());

            TimeUnit.SECONDS.sleep(INTERVAL);
        }
    }
}
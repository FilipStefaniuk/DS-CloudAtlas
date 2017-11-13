package pl.edu.mimuw.cloudatlas.fetcher;

import pl.edu.mimuw.cloudatlas.agent.AgentInterface;
import pl.edu.mimuw.cloudatlas.model.*;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Fetcher {

    private static final String HOST = "localhost";
    private static final String ZMI_NAME = "/uw/violet07";
    private static final String LOOKUP_NAME = "Agent";
    private static final Integer INTERVAL = 1;

    public static void main(String[] args) throws Exception {

        SystemInfo systemInfo = new SystemInfo();
        PathName pathName = new PathName(ZMI_NAME);
        Registry registry = LocateRegistry.getRegistry(HOST);
        AgentInterface stub = (AgentInterface) registry.lookup(LOOKUP_NAME);

        while (true) {
            systemInfo.updateAttributes();

            for (Map.Entry<Attribute, Value> entry : systemInfo.getAttributes())
                stub.setAttribute(pathName, entry.getKey(), entry.getValue());

            TimeUnit.SECONDS.sleep(INTERVAL);
        }
    }
}
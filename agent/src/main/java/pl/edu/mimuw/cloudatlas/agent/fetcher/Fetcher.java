package pl.edu.mimuw.cloudatlas.agent.fetcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.provider.ConfigurationProviderBuilder;
import org.cfg4j.source.classpath.ClasspathConfigurationSource;
import pl.edu.mimuw.cloudatlas.agent.AgentInterface;
import pl.edu.mimuw.cloudatlas.model.*;

import java.nio.file.Paths;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.TimeUnit;

public class Fetcher {

    private static Logger LOGGER = LogManager.getLogger(Fetcher.class);

    private static final String HOST = "localhost";
    private static final Integer INTERVAL = 1;

    public static void main(String[] args) throws Exception {

        LOGGER.info("Fetcher starting");


        if (args.length == 0) {
            throw new Exception();
        }

        ConfigurationProvider configurationProvider = new ConfigurationProviderBuilder()
                .withConfigurationSource(new ClasspathConfigurationSource(() -> Paths.get(args[0])))
                .build();

        String agentId = configurationProvider.getProperty("Agent.agentId", String.class);

        SystemInfo systemInfo = new SystemInfo();
        PathName pathName = new PathName(agentId);
        Registry registry = LocateRegistry.getRegistry(HOST);
        AgentInterface stub = (AgentInterface) registry.lookup(agentId);

        while (true) {
            systemInfo.updateAttributes();

            stub.setAttributes(pathName, systemInfo.getAttributes());

            TimeUnit.SECONDS.sleep(INTERVAL);
        }
    }
}
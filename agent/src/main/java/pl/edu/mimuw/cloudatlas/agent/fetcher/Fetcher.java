package pl.edu.mimuw.cloudatlas.agent.fetcher;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.provider.ConfigurationProviderBuilder;
import org.cfg4j.source.ConfigurationSource;
import org.cfg4j.source.classpath.ClasspathConfigurationSource;
import org.cfg4j.source.compose.MergeConfigurationSource;
import pl.edu.mimuw.cloudatlas.agent.Agent;
import pl.edu.mimuw.cloudatlas.agent.AgentInterface;
import pl.edu.mimuw.cloudatlas.model.*;

import java.nio.file.Paths;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.concurrent.TimeUnit;

public class Fetcher {

    private static Logger LOGGER = LogManager.getLogger(Fetcher.class);

    public static void main(String[] args) throws Exception {

        LOGGER.info("Fetcher starting");

        ConfigurationSource conf = new ClasspathConfigurationSource(() -> Paths.get(Agent.DEFAULT_CONFIGURATION));


        if (args.length > 0) {
            ConfigurationSource customConf = new ClasspathConfigurationSource(()-> Paths.get(args[0]));
            conf = new MergeConfigurationSource(conf, customConf);
        }

        ConfigurationProvider configurationProvider = new ConfigurationProviderBuilder()
                .withConfigurationSource(conf)
                .build();

        String agentId = configurationProvider.getProperty("Agent.agentId", String.class);
        String agentRMIname = configurationProvider.getProperty("Agent.RMIModule.rmiName", String.class);
        Integer interval = configurationProvider.getProperty("Fetcher.interval", Integer.class);

        SystemInfo systemInfo = new SystemInfo();
        PathName pathName = new PathName(agentId);
        Registry registry = LocateRegistry.getRegistry();
        AgentInterface stub = (AgentInterface) registry.lookup(agentRMIname);

        while (true) {
            systemInfo.updateAttributes();

            stub.setAttributes(pathName, systemInfo.getAttributes());

            TimeUnit.MILLISECONDS.sleep(interval);
        }
    }
}
package pl.edu.mimuw.cloudatlas.agent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cfg4j.provider.ConfigurationProvider;
import org.cfg4j.provider.ConfigurationProviderBuilder;
import org.cfg4j.source.classpath.ClasspathConfigurationSource;
import pl.edu.mimuw.cloudatlas.agent.fetcher.Fetcher;
import pl.edu.mimuw.cloudatlas.agent.framework.*;
import pl.edu.mimuw.cloudatlas.agent.modules.*;
import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.ValueQuery;
import pl.edu.mimuw.cloudatlas.model.ValueString;

import java.nio.file.Paths;

public class Agent {

    private static final Logger LOGGER = LogManager.getLogger(Agent.class);

    public static final String DEFAULT_CONFIGURATION = "cloudatlas.properties";

    public static void main(String[] args) throws Exception {

        String configurationFile;
        if (args.length > 0) {
            configurationFile = args[0];
        } else {
            configurationFile = DEFAULT_CONFIGURATION;
        }

        ConfigurationProvider configurationProvider = new ConfigurationProviderBuilder()
                .withConfigurationSource(new ClasspathConfigurationSource(() -> Paths.get(DEFAULT_CONFIGURATION)))
                .build();

        String agentId = configurationProvider.getProperty("Agent.agentId", String.class);
        Integer port = configurationProvider.getProperty("Agent.port", Integer.class);

        LOGGER.info("START: id = " + agentId + " port " + port.toString());

        EventQueue eq = EventQueue.builder(configurationFile)
                .executor(RMIModule.class)
                .executor(ZMIModule.class)
                .executor(GossipModule.class)
                .executor(InterpreterModule.class)
                .executor(CommunicationModule.class)
                .executor(TimerModule.class)
                .executor(VerifierModule.class)
                .build();

        Message message = new EmptyMessage();
        message.setAddress(new Address(GossipModule.class, GossipModule.INIT_GOSSIP));
        eq.sendMessage(message);

        AttributesMap attributesMap = new AttributesMap();
        attributesMap.addOrChange(new Attribute("&nmembers"), new ValueQuery("&nmembers", "SELECT sum(nmembers) AS nmembers"));
        attributesMap.addOrChange(new Attribute("&contacts"), new ValueQuery("&contacts", "SELECT random(10, distinct(unfold(contacts))) AS contacts"));
        Message message1 = new GenericMessage<>(attributesMap);
        message1.setAddress(new Address(ZMIModule.class, ZMIModule.ADD_OR_CHANGE_ATTRIBUTES));
        eq.sendMessage(message1);

        eq.start();

        String classpath = System.getProperty("java.class.path");
        ProcessBuilder processBuilder = new ProcessBuilder("java", "-cp", classpath, Fetcher.class.getName(), configurationFile);
        Process process = processBuilder.start();

        process.waitFor();

        System.out.println("Exit");
    }
}

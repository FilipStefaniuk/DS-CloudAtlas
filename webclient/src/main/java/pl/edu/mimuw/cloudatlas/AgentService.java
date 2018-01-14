package pl.edu.mimuw.cloudatlas;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import pl.edu.mimuw.cloudatlas.agent.AgentInterface;
import pl.edu.mimuw.cloudatlas.model.*;
import pl.edu.mimuw.cloudatlas.querySigner.QuerySignerInterface;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

@Component
public class AgentService {

    private static final Logger LOGGER = LogManager.getLogger(AgentService.class);

    private Registry registry;

    AgentService() throws Exception{
        registry = LocateRegistry.getRegistry();
    }

    List<String> retriveAllAgents() {
        try {
            LOGGER.info("retrive all agents");
            List<String> agents = new ArrayList<>();

            for (String name : registry.list()) {
                if (name.startsWith("agent")) {
                    agents.add(name);
                }
            }

            Collections.sort(agents);
            return agents;
        } catch (Exception e) {
            LOGGER.error(e);
            return new ArrayList<>();
        }
    }

    void setFallbackContacts(String agentId) {
        try {
            AgentInterface agent = (AgentInterface) registry.lookup(agentId);
            agent.setFallbackContacts(new HashSet<>());
        } catch (Exception e) {}
    }

    List<String> getZones(String agentId) {
        try {
            LOGGER.info("getZones");
            AgentInterface agent = (AgentInterface) registry.lookup(agentId);
            Set<PathName> pathNameSet = agent.getAgentZones();

            List<String> names = new ArrayList<>();
            for(PathName pathName : pathNameSet) {
                names.add(pathName.getName());
            }
            names.add("/");

            Collections.sort(names);

            return names;

        } catch (Exception e) {
            LOGGER.error("getZones: " + e, e);
        }
        return new ArrayList<>();
    }

    Map<String, String> getAttributes(String agentId, String pathName) {
        try {
            LOGGER.info("getAttributes: " + agentId + " " + pathName);
            pathName = pathName.replace("-", "/");
            AgentInterface agent = (AgentInterface) registry.lookup(agentId);

            AttributesMap attributes = agent.getAttributes(new PathName(pathName));

            Map<String, String> data = new HashMap<>();
            for(Map.Entry<Attribute, Value> entry : attributes) {
                data.put(entry.getKey().getName(), entry.getValue().toString());
            }
            return data;

        } catch (Exception e) {
            LOGGER.error("getAttributes: " + e.getMessage(), e);
        }
        return new HashMap<>();
    }

    void installQuery(String agentId, String name, String query) {
        try {
            LOGGER.info("installQuery: " + name + " " + query);
            QuerySignerInterface querySigner = (QuerySignerInterface) registry.lookup("cloudatlas_query_signer");

            ValueQuery valueQuery = querySigner.signQuery(name, query);

            AgentInterface agent = (AgentInterface) registry.lookup(agentId);

            agent.installQuery(new Attribute(name), valueQuery);
        } catch (Exception e) {
            LOGGER.error("installQuery: " + e.getMessage(), e);
        }
    }
}

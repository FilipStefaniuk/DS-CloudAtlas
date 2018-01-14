package pl.edu.mimuw.cloudatlas.agent.modules;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cfg4j.provider.ConfigurationProvider;
import pl.edu.mimuw.cloudatlas.agent.framework.*;
import pl.edu.mimuw.cloudatlas.interpreter.Interpreter;
import pl.edu.mimuw.cloudatlas.interpreter.QueryResult;
import pl.edu.mimuw.cloudatlas.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Module(value = "InterpreterModule", dependencies = {"ZMIModule"})
public class InterpreterModule extends ModuleBase{

    private static Logger LOGGER = LogManager.getLogger(InterpreterModule.class);

    static final int EXEC_QUERIES = 1;

    private String agentId;

    @Handler(EXEC_QUERIES)
    private final MessageHandler<?> h1 =  new MessageHandler<GenericMessage<ZMI>>() {

        @Override
        protected void handle(GenericMessage<ZMI> message) {

            LOGGER.debug("EXEC_QUERIES: IN: " + message.toString());

            try {
                Interpreter interpreter = new Interpreter(message.getData());

                AttributesMap attributesMap = new AttributesMap();
                List<QueryResult> queryResults = new ArrayList<>();

                AttributesMap discoveredQueries = new AttributesMap();
                for (ZMI son : message.getData().getSons()) {
                    String rep = ((ValueString) son.getAttributes().getOrNull(new Attribute(ZMIModule.REP))).getValue();
                    for (Map.Entry<Attribute, Value> entry : son.getAttributes()) {
                        if (entry.getKey().getName().startsWith("&")) {
                            if (rep.equals(agentId)) {
                                attributesMap.addOrChange(entry);
                            } else if (!entry.getKey().getName().equals(ZMIModule.Q_NMEMBERS)
                                    && !entry.getKey().getName().equals(ZMIModule.Q_CONTACTS)) {
                                ValueQuery query = (ValueQuery)message.getData().getAttributes().getOrNull(entry.getKey());
                                if (query == null || !query.equals(entry.getValue())) {
                                    discoveredQueries.addOrChange(entry);
                                }
                            }
                        }
                    }
                }

                if (!discoveredQueries.isEmpty()) {
                    Address address = new Address(VerifierModule.class, VerifierModule.VERIFY_QUERIES);
                    Message msg = new GenericMessage<>(discoveredQueries);
                    sendMessage(address, msg);
                    LOGGER.debug("EXECUTE_QUERIES: OUT: " + msg.toString());
                }

                for (Map.Entry<Attribute, Value> entry : attributesMap) {
                    String query = ((ValueQuery) entry.getValue()).getQuery();
                    if (!query.isEmpty()) {
                        queryResults.addAll(interpreter.executeQuery(query));
                    }
                }

                for(QueryResult result: queryResults) {
                    attributesMap.addOrChange(result.getName(), result.getValue());
                }

                attributesMap.addOrChange(ZMIModule.ID, message.getData().getAttributes().get(ZMIModule.ID));
                attributesMap.addOrChange(ZMIModule.REP, new ValueString(agentId));
                attributesMap.addOrChange(ZMIModule.ISSUED, new ValueInt(System.currentTimeMillis()));

                Address address = new Address(ZMIModule.class, ZMIModule.UPDATE_ATTRIBUTES);
                Message msg = new GenericMessage<>(attributesMap);
                sendMessage(address, msg);
                LOGGER.debug("EXEC_QUERIES: OUT: " + msg.toString());

            } catch (Exception e) {
                LOGGER.error("EXEC_QUERIES: " + e.getMessage(), e);
            }
        }
    };

    @Override
    public void initialize(ConfigurationProvider configurationProvider) {
        agentId = configurationProvider.getProperty("Agent.agentId", String.class);
    }
}

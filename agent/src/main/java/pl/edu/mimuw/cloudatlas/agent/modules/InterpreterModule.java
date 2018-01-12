package pl.edu.mimuw.cloudatlas.agent.modules;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cfg4j.provider.ConfigurationProvider;
import pl.edu.mimuw.cloudatlas.agent.framework.Handler;
import pl.edu.mimuw.cloudatlas.agent.framework.MessageHandler;
import pl.edu.mimuw.cloudatlas.agent.framework.Module;
import pl.edu.mimuw.cloudatlas.agent.framework.ModuleBase;
import pl.edu.mimuw.cloudatlas.agent.messages.AttributesMapMessage;
import pl.edu.mimuw.cloudatlas.agent.messages.ZMIRequestMessage;
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

    @Handler(EXEC_QUERIES)
    private final MessageHandler<?> h1 =  new MessageHandler<ZMIRequestMessage>() {

        @Override
        protected void handle(ZMIRequestMessage message) {

            LOGGER.debug("EXEC_QUERIES: IN: " + message.toString());

            try {
                Interpreter interpreter = new Interpreter(message.getZmi());

                List<QueryResult> queryResults = new ArrayList<>();
                for (Map.Entry<Attribute, Value> entry : message.getZmi().getAttributes()) {
                    if (entry.getKey().getName().startsWith("&")) {
                        String query = ((ValueString) entry.getValue()).getValue();
                        queryResults.addAll(interpreter.executeQuery(query));
                    }
                }

                AttributesMap attributesMap = new AttributesMap();
                for(QueryResult result: queryResults) {
                    attributesMap.addOrChange(result.getName(), result.getValue());
                }

                sendMessage(message.getResponseAddress(), new AttributesMapMessage(attributesMap, message.getPathName()));

            } catch (Exception e) {
                LOGGER.error("EXEC_QUERIES: " + e.getMessage(), e);
            }
        }
    };

    @Override
    public void initialize(ConfigurationProvider configurationProvider) {}
}

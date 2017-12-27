package pl.edu.mimuw.cloudatlas.agent.interpreter;

import pl.edu.mimuw.cloudatlas.agent.framework.MessageHandler;
import pl.edu.mimuw.cloudatlas.agent.framework.Module;
import pl.edu.mimuw.cloudatlas.interpreter.Interpreter;
import pl.edu.mimuw.cloudatlas.interpreter.QueryResult;
import pl.edu.mimuw.cloudatlas.model.*;

import java.util.List;
import java.util.Map;

public class InterpreterModule extends Module{

    public static final Integer EXEC_QUERIES = 1;

    public InterpreterModule() {

        registerHandler(EXEC_QUERIES, new MessageHandler<InterpretQueryMessage>() {
            @Override
            protected void handle(InterpretQueryMessage message) {
                try {
                    Interpreter interpreter = new Interpreter(message.getZmi());
//                    List<QueryResult> queryResults = interpreter.executeQuery(message.getQuery());
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

                    QueryResultMessage queryResultMessage = new QueryResultMessage(attributesMap, message.getPath());
                    sendMessage(message.getSenderId(), message.getSenderHandleId(), queryResultMessage);

                } catch (Exception e) {}
            }
        });
    }

}

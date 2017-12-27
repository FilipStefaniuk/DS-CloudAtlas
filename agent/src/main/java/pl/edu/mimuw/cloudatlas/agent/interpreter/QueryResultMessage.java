package pl.edu.mimuw.cloudatlas.agent.interpreter;

import pl.edu.mimuw.cloudatlas.agent.framework.Message;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;

public class QueryResultMessage extends Message{
    AttributesMap attributesMap;
    String path;

    public QueryResultMessage(AttributesMap attributesMap, String path) {
        this.attributesMap = attributesMap;
        this.path = path;
    }

    public AttributesMap getAttributesMap() {
        return attributesMap;
    }

    public String getPath() {
        return path;
    }
}

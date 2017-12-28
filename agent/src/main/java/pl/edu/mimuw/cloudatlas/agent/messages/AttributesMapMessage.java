package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.agent.framework.Message;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.PathName;

public class AttributesMapMessage extends Message {

    AttributesMap attributesMap;
    PathName pathName;

    public AttributesMapMessage(AttributesMap attributesMap, PathName pathName) {
        this.attributesMap = attributesMap;
        this.pathName = pathName;
    }

    public AttributesMap getAttributesMap() {
        return attributesMap;
    }

    public PathName getPathName() {
        return pathName;
    }
}

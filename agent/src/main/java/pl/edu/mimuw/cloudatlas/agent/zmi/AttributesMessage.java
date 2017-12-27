package pl.edu.mimuw.cloudatlas.agent.zmi;

import pl.edu.mimuw.cloudatlas.agent.framework.Message;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.PathName;

public class AttributesMessage extends Message{
    AttributesMap attributesMap;
    PathName pathName;

    public AttributesMessage(AttributesMap attributesMap, PathName pathName) {
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

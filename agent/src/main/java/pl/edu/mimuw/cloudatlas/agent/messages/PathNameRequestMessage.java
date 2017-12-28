package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.agent.framework.Address;
import pl.edu.mimuw.cloudatlas.agent.framework.RequestMessage;
import pl.edu.mimuw.cloudatlas.model.PathName;

public class PathNameRequestMessage extends RequestMessage {
    PathName pathName;

    public PathNameRequestMessage(Address responseAddress, PathName pathName) {
        super(responseAddress);
        this.pathName = pathName;
    }

    public PathName getPathName() {
        return pathName;
    }
}

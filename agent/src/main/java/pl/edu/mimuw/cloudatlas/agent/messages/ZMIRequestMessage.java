package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.agent.framework.Address;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.ZMI;

public class ZMIRequestMessage extends PathNameRequestMessage {

    ZMI zmi;

    public ZMIRequestMessage(Address responseAddress, PathName pathName, ZMI zmi) {
        super(responseAddress, pathName);
        this.zmi = zmi;
    }

    public ZMI getZmi() {
        return zmi;
    }
}

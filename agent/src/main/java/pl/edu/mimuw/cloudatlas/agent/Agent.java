package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.model.*;

import java.rmi.RemoteException;

public class Agent implements AgentInterface {

    private ZMI root;

    public Agent(String name) {
        PathName pathName = new PathName(name);
        ZMI zmi = null;
        for (String component : pathName.getComponents()) {
            ZMI new_zmi = createInitialZMI(zmi, component);
            if (zmi == null) {
                root = new_zmi;
            }
            zmi = new_zmi;
        }
    }

    private ZMI createInitialZMI(ZMI parent, String name) {
        ZMI zmi = parent != null ? new ZMI(parent) : new ZMI();
        zmi.getAttributes().add("name", new ValueString(name));
        return zmi;
    }

    @Override
    public void setValue(PathName name, Attribute attribute, Value value) throws RemoteException {

    }

    @Override
    public AttributesMap getValues(PathName name) throws RemoteException {
        return null;
    }
}

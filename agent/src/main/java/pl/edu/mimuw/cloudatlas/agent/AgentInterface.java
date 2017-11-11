package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.model.Attribute;
import pl.edu.mimuw.cloudatlas.model.AttributesMap;
import pl.edu.mimuw.cloudatlas.model.PathName;
import pl.edu.mimuw.cloudatlas.model.Value;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AgentInterface extends Remote {

    /**
     * Sets the values of attributes of a given zone (operation is allowed only for the singleton zones).
     * @param name a name of a zone
     * @param attribute a name of an attribute
     * @param value a value of an attribute
     * @throws RemoteException
     */
    public void setValue(PathName name, Attribute attribute, Value value) throws RemoteException;

    /**
     * Returns the values of attributes of a given zone
     * @param name a name of a zone
     * @return values of the attributes in the zone's ZMI
     * @throws RemoteException
     */
    public AttributesMap getValues(PathName name) throws RemoteException;
}

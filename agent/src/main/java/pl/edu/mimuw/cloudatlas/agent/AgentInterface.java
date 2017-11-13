package pl.edu.mimuw.cloudatlas.agent;


import pl.edu.mimuw.cloudatlas.model.*;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface AgentInterface extends Remote {
    public void setAttribute(PathName pathName, Attribute attribute, Value value) throws RemoteException;
    public AttributesMap getAttributes(PathName pathName) throws RemoteException;
    public void installQuery(Attribute attribute, ValueString value) throws RemoteException;
    public void uninstallQuery(Attribute attribute, ValueString value) throws RemoteException;
    public Set<PathName> getAgentZones() throws RemoteException;
    public void setFallbackContacts(Set<ValueContact> contacts) throws RemoteException;
}

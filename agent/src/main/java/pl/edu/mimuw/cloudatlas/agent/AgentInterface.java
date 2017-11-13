package pl.edu.mimuw.cloudatlas.agent;


import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AgentInterface extends Remote {
    public void setValue(int value) throws RemoteException;
}

package pl.edu.mimuw.cloudatlas.querySigner;

import pl.edu.mimuw.cloudatlas.model.ValueQuery;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface QuerySignerInterface extends Remote {
    public ValueQuery signQuery(String name, String query) throws RemoteException;
}

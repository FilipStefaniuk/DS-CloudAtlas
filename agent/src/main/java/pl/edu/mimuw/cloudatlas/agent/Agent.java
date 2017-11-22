package pl.edu.mimuw.cloudatlas.agent;

import pl.edu.mimuw.cloudatlas.interpreter.InterpreterMain;
import pl.edu.mimuw.cloudatlas.model.*;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

public class Agent implements AgentInterface {

    private ZMI root;
    private Set<ValueContact> fallbackContacts;


    public Agent() throws Exception {
        root = InterpreterMain.createDefaultInterpreterHierarchy();
        fallbackContacts = new HashSet<>();
    }

    ZMI getZMIByPathName(PathName pathName) throws Exception{

        ZMI result = root;
        Boolean isNew = false;

        for(String name : pathName.getComponents()) {
            for(ZMI son : result.getSons()) {
                if (((ValueString) son.getAttributes().get("name")).getValue().equals(name)) {
                    result = son;
                    isNew = true;
                    break;
                }
            }

            if(!isNew)
                throw new Exception();

            isNew = false;

        }
        return result;
    }

    public void update() throws Exception {
        try {
            InterpreterMain.execute(root);
        } catch (Exception e) {}
    }

    @Override
    public AttributesMap getAttributes(PathName pathName) throws RemoteException {
        try {
            return getZMIByPathName(pathName).getAttributes();
        } catch (Exception e) {
            throw new RemoteException();
        }
    }

    @Override
    public void setAttribute(PathName pathName, Attribute attribute, Value value) throws RemoteException{
        try {
            ZMI zmi = getZMIByPathName(pathName);
            if (!zmi.getSons().isEmpty())
                throw new Exception();
            zmi.getAttributes().addOrChange(attribute, value);
        } catch (Exception e) {
            throw new RemoteException();
        }
    }

    @Override
    public void installQuery(Attribute attribute, ValueString value) throws RemoteException {
        try {
            InterpreterMain.installQuery(root, attribute, value);
        } catch (Exception e) {
            throw new RemoteException();
        }
    }


    @Override
    public void uninstallQuery(Attribute attribute) throws RemoteException {
        try {
            InterpreterMain.uninstallQuery(root, attribute);
        } catch (Exception e) {
            throw new RemoteException();
        }
    }

    @Override
    public Set<PathName> getAgentZones() throws RemoteException {
        return getNames(root);
    }

    private Set<PathName> getNames(ZMI zmi) {

        Set<PathName> pathNames = new HashSet<>();
        pathNames.add(InterpreterMain.getPathName(zmi));

        if (!zmi.getSons().isEmpty())
            for(ZMI son : zmi.getSons())
                pathNames.addAll(getNames(son));

        return pathNames;
    }

    @Override
    public void setFallbackContacts(Set<ValueContact> contacts) throws RemoteException {
        fallbackContacts = contacts;
    }
}

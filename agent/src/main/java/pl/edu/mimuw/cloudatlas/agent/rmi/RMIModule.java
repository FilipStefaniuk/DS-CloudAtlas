package pl.edu.mimuw.cloudatlas.agent.rmi;

import pl.edu.mimuw.cloudatlas.agent.AgentInterface;
import pl.edu.mimuw.cloudatlas.agent.framework.Module;
import pl.edu.mimuw.cloudatlas.agent.zmi.AttributesMessage;
import pl.edu.mimuw.cloudatlas.agent.zmi.ZmiModule;
import pl.edu.mimuw.cloudatlas.model.*;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class RMIModule extends Module {

    private static final Integer GET_ATTRIBUTES = 1;
    private static final Integer SET_ATTRIBUTES = 2;
    private static final Integer INSTALL_QUERY = 3;
    private static final Integer UNINSTALL_QUERY = 4;
    private static final Integer GET_ZONES = 5;
    private static final Integer SET_CONTACTS = 6;

    private Integer zmiModuleId;

    private AgentInterface server = new AgentInterface() {
        @Override
        public void setAttributes(PathName pathName, AttributesMap attributesMap) throws RemoteException {
            try {
            sendMessage(zmiModuleId, ZmiModule.UPDATE_ATTRIBUTES, new AttributesMessage(attributesMap, pathName));
            } catch (Exception e) {}
        }

        @Override
        public AttributesMap getAttributes(PathName pathName) throws RemoteException {
            return null;
        }

        @Override
        public void installQuery(Attribute attribute, ValueString value) throws RemoteException {}

        @Override
        public void uninstallQuery(Attribute attribute) throws RemoteException {}

        @Override
        public Set<PathName> getAgentZones() throws RemoteException {
            return null;
        }

        @Override
        public void setFallbackContacts(Set<ValueContact> contacts) throws RemoteException {
            try {
                sendMessage();
            }
        }
    };


    public RMIModule(ZmiModule zmiModule) {
        super(new HashSet<>(Arrays.asList(zmiModule)));

        zmiModuleId = zmiModule.getId();


    }

}

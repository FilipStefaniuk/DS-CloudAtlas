package pl.edu.mimuw.cloudatlas.agent;

//import pl.edu.mimuw.cloudatlas.interpreter.InterpreterMain;
//import pl.edu.mimuw.cloudatlas.model.*;
//
//import java.rmi.RemoteException;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.Iterator;
//import java.util.List;

public class Agent implements AgentInterface{

//    private ZMI root;

    private int value;

    public Agent() throws Exception {
//        root = InterpreterMain.createDefaultInterpreterHierarchy();
        this.value = 0;
    }

//    ZMI getZMIByPathName(PathName pathName) throws Exception{
//
//        ZMI result = root;
//        Boolean isNew = false;
//
//        for(String name : pathName.getComponents()) {
//            for(ZMI son : result.getSons()) {
//                if (((ValueString) son.getAttributes().get("name")).getValue().equals(name)) {
//                    result = son;
//                    isNew = true;
//                    break;
//                }
//            }
//
//            if(!isNew)
//                throw new Exception();
//
//            isNew = false;
//
//        }
//        return result;
//    }

//    public AttributesMap getAttributesOfZone(PathName pathName) throws Exception{
//        return getZMIByPathName(pathName).getAttributes();
//    }

    public void setValue(int value) {
        System.out.println(value);
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
}

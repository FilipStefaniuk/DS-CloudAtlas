package pl.edu.mimuw.cloudatlas.agent.framework;

import java.io.Serializable;

public abstract class Message implements Serializable {

    private Address address;

    public void setAddress(Address address) {
        this.address = address;
    }

    public Address getAddress() {
        return address;
    }
}

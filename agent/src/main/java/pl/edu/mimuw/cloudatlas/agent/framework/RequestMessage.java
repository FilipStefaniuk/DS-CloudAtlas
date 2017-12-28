package pl.edu.mimuw.cloudatlas.agent.framework;

public class RequestMessage extends Message {

    private Address responseAddress;

    public RequestMessage(Address responseAddress) {
        this.responseAddress = responseAddress;
    }

    public Address getResponseAddress() {
        return responseAddress;
    }
}

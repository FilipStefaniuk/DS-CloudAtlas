package pl.edu.mimuw.cloudatlas.agent.framework;

public abstract class MessageWithResponse extends Message {

    private Address responseAddress;

    public MessageWithResponse(Address responseAddress) {
        this.responseAddress = responseAddress;
    }

    public Address getResponseAddress() {
        return responseAddress;
    }
}

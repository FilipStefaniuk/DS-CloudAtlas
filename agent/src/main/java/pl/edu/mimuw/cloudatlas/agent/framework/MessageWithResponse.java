package pl.edu.mimuw.cloudatlas.agent.framework;

public abstract class MessageWithResponse extends Message {
    private Integer SenderID;
    private Integer HandlerID;

    public MessageWithResponse(Integer senderID, Integer handlerID) {
        SenderID = senderID;
        HandlerID = handlerID;
    }

    public Integer getSenderID() {
        return SenderID;
    }

    public Integer getHandlerID() {
        return HandlerID;
    }
}

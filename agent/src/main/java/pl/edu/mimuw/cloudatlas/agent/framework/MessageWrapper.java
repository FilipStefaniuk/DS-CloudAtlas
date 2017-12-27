package pl.edu.mimuw.cloudatlas.agent.framework;

public class MessageWrapper <T extends Message>{
    private Integer source;
    private Integer destination;
    private Integer type;
    private T message;

    public MessageWrapper(Integer src, Integer dst, Integer type, T message) {
        this.source = src;
        this.destination = dst;
        this.type = type;
        this.message = message;
    }

    public Integer getSource() {
        return source;
    }

    public Integer getDestination() {
        return destination;
    }

    public T getMessage() {
        return message;
    }

    public Integer getType() {
        return type;
    }
}

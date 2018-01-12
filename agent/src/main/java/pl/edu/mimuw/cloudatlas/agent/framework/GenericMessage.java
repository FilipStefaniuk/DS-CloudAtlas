package pl.edu.mimuw.cloudatlas.agent.framework;

import java.io.Serializable;

public class GenericMessage<T extends Serializable> extends Message {
    private T data;

    public GenericMessage(T value) {
        this.data = value;
    }

    public T getData() {
        return data;
    }

    @Override
    public String toString() {
        return "GenericMessage{" +
                "data=" + data +
                '}';
    }
}

package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.agent.framework.Message;

public class IdMessage extends Message {
    Integer id;

    public IdMessage(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}

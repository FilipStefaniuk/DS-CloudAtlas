package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.agent.framework.Message;
import pl.edu.mimuw.cloudatlas.agent.modules.CommunicationModule;

public class IdMessage extends Message {
    CommunicationModule.PartialMessageId id;

    public IdMessage(CommunicationModule.PartialMessageId id) {
        this.id = id;
    }

    public CommunicationModule.PartialMessageId getId() {
        return id;
    }
}

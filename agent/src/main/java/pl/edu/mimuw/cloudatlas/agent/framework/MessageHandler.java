package pl.edu.mimuw.cloudatlas.agent.framework;

public abstract class MessageHandler <T extends Message> {

    public final void handleMessage(Message message) {

        @SuppressWarnings("unchecked")
        T typedMessage = ((T) message);

        handle(typedMessage);
    }

    protected abstract void handle(T message);
}


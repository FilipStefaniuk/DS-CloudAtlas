package pl.edu.mimuw.cloudatlas.agent.framework;

public abstract class MessageHandler <T extends Message> {

    final void handleMessage(Message message) {
        try {
            @SuppressWarnings("unchecked")
            T typedMessage = ((T) message);

            handle(typedMessage);

        // Add logging here (warning/error)
        } catch (ClassCastException e) {}
    }

    protected abstract void handle(T message);
}


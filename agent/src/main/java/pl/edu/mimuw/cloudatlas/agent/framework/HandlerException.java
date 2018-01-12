package pl.edu.mimuw.cloudatlas.agent.framework;

public class HandlerException extends Exception {

    public HandlerException(String message) {
        super(message);
    }

    public HandlerException(String message, Throwable throwable) {
        super(message, throwable);
    }
}

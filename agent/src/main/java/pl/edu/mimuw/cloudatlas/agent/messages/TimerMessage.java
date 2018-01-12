package pl.edu.mimuw.cloudatlas.agent.messages;

import pl.edu.mimuw.cloudatlas.agent.framework.Message;

public class TimerMessage extends Message {
    private Long id;
    private String author;
    private Long startTime;
    private Long delay;
    private Message message;

    public TimerMessage(Long id, String author, Long startTime, Long delay, Message message) {
        this.id = id;
        this.author = author;
        this.startTime = startTime;
        this.delay = delay;
        this.message = message;
    }

    public Long getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public Long getStartTime() {
        return startTime;
    }

    public Long getDelay() {
        return delay;
    }

    public Message getMessage() {
        return message;
    }
}

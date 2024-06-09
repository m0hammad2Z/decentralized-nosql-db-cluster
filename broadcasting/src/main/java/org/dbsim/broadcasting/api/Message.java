package org.dbsim.broadcasting.api;

public class Message<T> {
    private T content;

    public Message(T content) {
        this.content = content;
    }

    public Message() {
        content = (T) new Object();
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }
}

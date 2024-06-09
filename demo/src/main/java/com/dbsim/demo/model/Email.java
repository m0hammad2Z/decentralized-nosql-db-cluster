package com.dbsim.demo.model;

public class Email {
    private int id;
    private String Sender;
    private String Receiver;
    private String Subject;
    private String Body;

    public Email(int id, String sender, String receiver, String subject, String body) {
        this.id = id;
        this.Sender = sender;
        this.Receiver = receiver;
        this.Subject = subject;
        this.Body = body;
    }

    public Email(String sender, String receiver, String subject, String body) {
        this.Sender = sender;
        this.Receiver = receiver;
        this.Subject = subject;
        this.Body = body;
    }

    public Email() {
    }

    public String getSender() {
        return Sender;
    }

    public void setSender(String sender) {
        this.Sender = sender;
    }

    public String getReceiver() {
        return Receiver;
    }

    public void setReceiver(String receiver) {
        this.Receiver = receiver;
    }

    public String getSubject() {
        return Subject;
    }

    public void setSubject(String subject) {
        this.Subject = subject;
    }

    public String getBody() {
        return Body;
    }

    public void setBody(String body) {
        this.Body = body;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

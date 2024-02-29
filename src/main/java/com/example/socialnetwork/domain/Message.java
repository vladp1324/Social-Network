package com.example.socialnetwork.domain;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message extends Entity<Long> {
    private User from;
    private User to;
    private String text;
    private LocalDateTime dateTime;
    private Long idreply;

    public Message(User from, User to, String text) {
        this.from = from;
        this.to = to;
        this.text = text;
        this.dateTime = LocalDateTime.now();
        this.idreply = null;
    }

    public Message(User from, User to, String text, LocalDateTime dateTime) {
        this.from = from;
        this.to = to;
        this.text = text;
        this.dateTime = dateTime;
    }

    public Message(User from, User to, String text, Long idreply) {
        this.from = from;
        this.to = to;
        this.text = text;
        this.dateTime = LocalDateTime.now();
        this.idreply = idreply;
    }

    public Message(User from, User to, String text, LocalDateTime dateTime, Long idreply) {
        this.from = from;
        this.to = to;
        this.text = text;
        this.dateTime = dateTime;
        this.idreply = idreply;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public User getFrom() {
        return from;
    }

    public User getTo() {
        return to;
    }

    public String getText() {
        return text;
    }

    public Long getIdreply() {
        return idreply;
    }

    public void setIdreply(Long idreply) {
        this.idreply = idreply;
    }

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public String toString() {
        return this.getId() + " " + dateTime.format(formatter) + " " + from.getUsername() + ": " +
                text + " " + idreply;
    }
}
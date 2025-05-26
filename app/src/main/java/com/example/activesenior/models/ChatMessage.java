package com.example.activesenior.models;

import java.util.Date;

public class ChatMessage {
    private String message;
    private boolean isUser;
    private Date timestamp;
    private boolean isDateHeader;

    public ChatMessage(String message, boolean isUser, Date timestamp, boolean isDateHeader) {
        this.message = message;
        this.isUser = isUser;
        this.timestamp = timestamp;
        this.isDateHeader = isDateHeader;
    }

    public String getMessage() {
        return message;
    }

    public boolean isUser() {
        return isUser;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public boolean isDateHeader() {
        return isDateHeader;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setUser(boolean user) {
        isUser = user;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setDateHeader(boolean dateHeader) {
        isDateHeader = dateHeader;
    }
}

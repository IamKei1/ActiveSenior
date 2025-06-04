package com.example.activesenior.models;

import java.util.Date;
import java.util.List;

public class ChatMessage {
    private String message;
    private Date timestamp;
    private boolean isDateHeader;
    private String senderId;
    private String receiverId;
    private List<String> readBy;

    public ChatMessage() {}


    // 기존 생성자
    public ChatMessage(String message, Date timestamp, boolean isDateHeader) {
        this.message = message;
        this.timestamp = timestamp;
        this.isDateHeader = isDateHeader;
    }

    // 새로운 생성자: senderId/receiverId까지 포함
    public ChatMessage(String message, Date timestamp, boolean isDateHeader,
                       String senderId, String receiverId) {
        this.message = message;
        this.timestamp = timestamp;
        this.isDateHeader = isDateHeader;
        this.senderId = senderId;
        this.receiverId = receiverId;
    }

    // Getter/Setter
    public String getMessage() { return message; }
    public Date getTimestamp() { return timestamp; }
    public boolean isDateHeader() { return isDateHeader; }
    public String getSenderId() { return senderId; }
    public String getReceiverId() { return receiverId; }
    public List<String> getReadBy() {
        return readBy;
    }

    public void setMessage(String message) { this.message = message; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
    public void setDateHeader(boolean dateHeader) { isDateHeader = dateHeader; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }
    public void setReadBy(List<String> readBy) {
        this.readBy = readBy;
    }
}

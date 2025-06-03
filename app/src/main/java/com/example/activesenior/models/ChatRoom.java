package com.example.activesenior.models;

import java.util.Date;

public class ChatRoom {
    private String roomId;
    private String participant1Id;
    private String participant2Id;
    private String participant1Name;
    private String participant2Name;
    private String lastMessage;
    private Date lastTimestamp;

    // Firestore 역직렬화용 기본 생성자
    public ChatRoom() {}

    public ChatRoom(String roomId, String participant1Id, String participant2Id,
                    String participant1Name, String participant2Name,
                    String lastMessage, Date lastTimestamp) {
        this.roomId = roomId;
        this.participant1Id = participant1Id;
        this.participant2Id = participant2Id;
        this.participant1Name = participant1Name;
        this.participant2Name = participant2Name;
        this.lastMessage = lastMessage;
        this.lastTimestamp = lastTimestamp;
    }

    // Getters & Setters
    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getParticipant1Id() {
        return participant1Id;
    }

    public void setParticipant1Id(String participant1Id) {
        this.participant1Id = participant1Id;
    }

    public String getParticipant2Id() {
        return participant2Id;
    }

    public void setParticipant2Id(String participant2Id) {
        this.participant2Id = participant2Id;
    }

    public String getParticipant1Name() {
        return participant1Name;
    }

    public void setParticipant1Name(String participant1Name) {
        this.participant1Name = participant1Name;
    }

    public String getParticipant2Name() {
        return participant2Name;
    }

    public void setParticipant2Name(String participant2Name) {
        this.participant2Name = participant2Name;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Date getLastTimestamp() {
        return lastTimestamp;
    }

    public void setLastTimestamp(Date lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
    }
}

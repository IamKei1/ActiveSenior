package com.example.activesenior.models;


import java.util.Date;

public class ChatRoom {
    private String roomId;
    private String participantName;
    private String participantUid;
    private String lastMessage;
    private Date lastTimestamp;


    // 기본 생성자 (Firestore 역직렬화용)
    public ChatRoom() {}

    public ChatRoom(String roomId, String participantName, String participantUid,
                    String lastMessage, Date lastTimestamp) {
        this.roomId = roomId;
        this.participantName = participantName;
        this.participantUid = participantUid;
        this.lastMessage = lastMessage;
        this.lastTimestamp = lastTimestamp;
    }

    // Getter & Setter
    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getParticipantName() {
        return participantName;
    }

    public void setParticipantName(String participantName) {
        this.participantName = participantName;
    }

    public String getParticipantUid() {
        return participantUid;
    }

    public void setParticipantUid(String participantUid) {
        this.participantUid = participantUid;
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

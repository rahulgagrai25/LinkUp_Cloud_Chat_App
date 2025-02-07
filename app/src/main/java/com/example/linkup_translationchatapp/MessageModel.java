package com.example.linkup_translationchatapp;

public class MessageModel {

    String Message, SenderID;
    Long timestamp;

    public MessageModel(String message, String senderID, Long timestamp) {
        this.Message = message;
        this.SenderID = senderID;
        this.timestamp = timestamp;
    }

    public MessageModel() {
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getSenderID() {
        return SenderID;
    }

    public void setSenderID(String senderID) {
        SenderID = senderID;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}

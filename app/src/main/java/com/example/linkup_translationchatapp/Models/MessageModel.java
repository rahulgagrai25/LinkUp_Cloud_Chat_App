package com.example.linkup_translationchatapp.Models;

public class MessageModel {
    private String senderId;
    private String messageText;
    private long timestamp;

    public MessageModel() {
    }

    public MessageModel(String senderId, String messageText, long timestamp) {
        this.senderId = senderId;
        this.messageText = messageText;
        this.timestamp = timestamp;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getMessageText() {
        return messageText;
    }

    public long getTimestamp() {
        return timestamp;
    }
}

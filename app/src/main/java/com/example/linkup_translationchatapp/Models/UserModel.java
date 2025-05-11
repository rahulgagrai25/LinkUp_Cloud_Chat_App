package com.example.linkup_translationchatapp.Models;

public class UserModel {
    public String userId;
    public String name;
    public String email;

    public UserModel() {

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserModel(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
    }
}

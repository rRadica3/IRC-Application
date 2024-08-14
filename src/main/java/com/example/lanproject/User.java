package com.example.lanproject;

import java.io.Serializable;
import java.util.Arrays;

public class User implements Serializable {
    private String username;
    private String password;
    private boolean admin;
    private String token;
    private boolean online;

    public User() {
    }

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
    public User(String username, String token, boolean admin) {
        this.username = username;
        this.password = password;
        this.admin = admin;
    }
    public User(String username, String password, boolean admin, String token, boolean online) {
        this.username = username;
        this.password = password;
        this.admin = admin;
        this.token = token;
        this.online = online;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }


    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", admin=" + admin + '\'' +
                ", token= " + token + '\'' +
                '}';
    }


}


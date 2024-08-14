package com.example.lanproject;

import java.io.Serializable;

public class Interaction implements Serializable {
    private String username;
    private String password;
    private String token;
    private String target;
    private String payload;
    private boolean admin;
    private boolean online;
    private boolean valid;
    private int action;
    //ACTION 0: Create new account
    //ACTION 1: Log into existing account
    //ACTION 2: Log out of account
    //ACTION 3: Send message

    public Interaction() {
    }

    public Interaction(String username, String password, int action) {
        this.username = username;
        this.password = password;
        this.action = action;
    }

    public Interaction(String token, boolean admin, boolean valid) {
        this.token = token;
        this.admin = admin;
        this.valid = valid;
    }

    public Interaction(String username, String token, String target, String payload, int action) {
        this.username = username;
        this.token = token;
        this.target = target;
        this.payload = payload;
        this.action = action;
    }

    public Interaction(String username, String password, String token, boolean admin, boolean online) {
        this.username = username;
        this.password = password;
        this.token = token;
        this.admin = admin;
        this.online = online;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }
}


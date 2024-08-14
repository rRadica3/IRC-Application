package com.example.lanproject;

import java.io.Serializable;

public class ClientAction implements Serializable {
    private String username;
    private String sessionToken;
    private int action;
    private String target;
    private String payload;

    public ClientAction(String username, String sessionToken, int action, String target, String payload) {
        this.username = username;
        this.sessionToken = sessionToken;
        this.action = action;
        this.target = target;
        this.payload = payload;
    }

    public ClientAction(String username, String sessionToken, int action) {
        this.username = username;
        this.sessionToken = sessionToken;
        this.action = action;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
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
}

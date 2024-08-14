package com.example.lanproject;

import java.io.Serializable;

public class ServerAction implements Serializable {
    private String username;
    private int action;
    private String payload;

    public ServerAction(String username, int action, String payload) {
        this.username = username;
        this.action = action;
        this.payload = payload;
    }

    public String getUsername() {
        return username;
    }

    public int getAction() {
        return action;
    }

    public String getPayload() {
        return payload;
    }

}

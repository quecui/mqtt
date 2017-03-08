package com.mqtt.model;

/**
 * Created by StormSpirit on 11/21/2016.
 */
public class Connect {
    private int type;
    private int clientID;
    private String username;

    public Connect() {
        this.type = 1;
    }

    public int getClientID() {
        return clientID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public int getType() {
        return type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}

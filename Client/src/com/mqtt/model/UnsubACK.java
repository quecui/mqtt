package com.mqtt.model;

/**
 * Created by StormSpirit on 11/21/2016.
 */
public class UnsubACK {
    private int type;
    private int packetID; // == clientID chanel

    public UnsubACK() {
        this.type = 8;
    }

    public int getType() {
        return type;
    }

    public int getPacketID() {
        return packetID;
    }

    public void setPacketID(int packetID) {
        this.packetID = packetID;
    }
}

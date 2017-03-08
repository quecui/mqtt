package com.mqtt.model;

/**
 * Created by StormSpirit on 11/21/2016.
 */
public class SubcribeACK {
    private int type;
    private int packetID;

    public SubcribeACK() {
        this.type = 6;
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

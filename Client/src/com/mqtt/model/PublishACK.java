package com.mqtt.model;

/**
 * Created by StormSpirit on 11/21/2016.
 */
public class PublishACK {
    private int type;
    private int packetID;

    public PublishACK() {
        this.type = 4;
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

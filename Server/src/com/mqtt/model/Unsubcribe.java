package com.mqtt.model;

/**
 * Created by StormSpirit on 11/21/2016.
 */
public class Unsubcribe {
    private int type;
    private int packetID; // == clientID chanel

    public Unsubcribe() {
        this.type = 7;
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

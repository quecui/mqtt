package com.mqtt.model;

import java.util.Random;

/**
 * Created by StormSpirit on 11/21/2016.
 */
public class Publish {
    private int type;
    private int packetID;
    private String nameChanel;
    private String message; // notify to all subcribe
    private String payload; // contain fileSize


    public Publish() {
        this.type = 3;
        Random rn = new Random();
        this.packetID = rn.nextInt(300);
    }

    public void setPacketID(int packetID) {
        this.packetID = packetID;
    }

    public int getPacketID() {
        return packetID;
    }

    public int getType() {
        return type;
    }

    public String getNameChanel() {
        return nameChanel;
    }

    public void setNameChanel(String nameChanel) {
        this.nameChanel = nameChanel;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}

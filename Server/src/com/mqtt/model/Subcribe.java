package com.mqtt.model;

/**
 * Created by StormSpirit on 11/21/2016.
 */
public class Subcribe {
    private int type;
    private int topicID; // == clientID

    public Subcribe() {
        this.type = 5;
    }

    public int getType() {
        return type;
    }

    public int getTopicID() {
        return topicID;
    }

    public void setTopicID(int topicID) {
        this.topicID = topicID;
    }
}

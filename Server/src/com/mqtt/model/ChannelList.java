package com.mqtt.model;

import java.util.List;

/**
 * Created by StormSpirit on 11/21/2016.
 */
public class ChannelList {
    private int type;
    private List<Channel> channels;

    public ChannelList(){
        this.type = 9;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }
}

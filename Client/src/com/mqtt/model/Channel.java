package com.mqtt.model;

import java.util.List;

/**
 * Created by StormSpirit on 11/21/2016.
 */
public class Channel {
    private int type;
    private int clientID;
    private String nameChanel;

    private List<String> listfile;

    public Channel(){
        this.type = 10;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getClientID() {
        return clientID;
    }

    public void setClientID(int clientID) {
        this.clientID = clientID;
    }

    public String getNameChanel() {
        return nameChanel;
    }

    public void setNameChanel(String nameChanel) {
        this.nameChanel = nameChanel;
    }

    public List<String> getListfile() {
        return listfile;
    }

    public void setListfile(List<String> listfile) {
        this.listfile = listfile;
    }
}

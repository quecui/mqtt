package com.mqtt.model;

/**
 * Created by StormSpirit on 11/21/2016.
 */
public class ConnectACK {
    private int type;
    private int returnCode;

    public ConnectACK() {
        this.type = 2;
    }

    public int getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}

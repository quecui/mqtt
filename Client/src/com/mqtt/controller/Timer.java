package com.mqtt.controller;

import java.util.Set;

/**
 * Created by StormSpirit on 12/3/2016.
 */
public class Timer extends Thread {
    public void run(){
        try {
            Thread.sleep(75000);
        }catch (Exception e){
        }
    }

    public boolean checkThreadAlive(String name){
        Set<Thread> threadSet = Thread.getAllStackTraces().keySet();
        Thread[] threadArray = threadSet.toArray(new Thread[threadSet.size()]);

        for(int i = 0; i < threadArray.length; i++){
            if(threadArray[i].getName().equals(name)){
                if(threadArray[i].isAlive())
                    return true;
                return false;
            }
        }

        return false;
    }
}

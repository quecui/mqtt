package com.mqtt.controller;

import com.mqtt.model.Publish;
import com.mqtt.services.Services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by StormSpirit on 11/24/2016.
 */
public class AutoListenner extends Thread {

    Socket sk;
    int clientID;

    public AutoListenner(Socket socket, int clientID){
        this.sk = socket;
        this.clientID = clientID;
    }

    public void run(){
        try {
            InetAddress ip = InetAddress.getLocalHost();
            Services services = new Services(sk);
            ServerSocket serverSocket = new ServerSocket((clientID + 2000), 50, ip);
            String jsonMessage;

            foo:
            while (true){
                Socket socket = serverSocket.accept();
               // System.out.println("AutoListenner: " + socket.getInetAddress());
                BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while((jsonMessage = br.readLine()) == null);

                String key = services.getTypeMessage(jsonMessage);
                switch (key){
                    case "3":
                        services.getData(socket, jsonMessage); //publish.getPacketID(), publish.getNameChanel().split("/")[1],
                        services.processPublishReturn(jsonMessage);
                        //System.out.println("======= " + publish.getMessage() + " =======");
                        socket.close();
                        continue foo;
                    case "10":

                    default:
                        break ;

                }
            }

        }catch (Exception e){
            System.out.println("Error at AutoListenner: " + e.getMessage());
        }
    }
}

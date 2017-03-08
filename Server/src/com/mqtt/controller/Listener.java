package com.mqtt.controller;

import com.mqtt.model.Connect;
import com.mqtt.model.Publish;
import com.mqtt.services.Services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by StormSpirit on 11/24/2016.
 */
public class Listener extends Thread {
    Socket socket;
    Services services;

    public Listener(Socket socket) throws IOException {
        this.socket = socket;
        this.services = new Services(socket);
    }

    public void run() {

        int clientID = -1;
        try {
            services.createResources();
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String jsonMessage;
            Connect connectMessage = new Connect();
            foo:
            while (true) {
                while ((jsonMessage = br.readLine()) == null) ;

                String key = services.getTypeMessage(jsonMessage);
                System.out.println(jsonMessage);

                switch (key) {
                    case "1":
                        connectMessage = services.processConnectMessage(jsonMessage);
                        if (connectMessage != null) {
                            clientID = connectMessage.getClientID();
                            Thread.sleep(2000);
                            services.sendListChannelToClient();
                        }

                        break;
                    case "3":
                        Publish publish = services.processPublishMessage(jsonMessage);
                        //todo: send publish message to all subcribe
                        break;
                    case "5":
                        services.processSubcribeMessage(jsonMessage);
                        services.sendListChannelToClient();
                        break;
                    case "7":
                        services.processUnsubcribeMessage(jsonMessage);
                        break;
                    case "9":
                        services.getRequiredChannel(jsonMessage);
                        break;
                    case "11":
                        services.sendListChannelToClient();
                        break ;
                    default:

                }
            }

        } catch (Exception e) {
            System.out.println("Error at Listener: " + e.getMessage());
            System.out.println("Socket is closed");
            try {
                socket.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } finally {
            if (socket.isClosed()) {
                try {
                    services.deleteClientID(clientID);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

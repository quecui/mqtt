package com.mqtt;

import com.mqtt.controller.Listener;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;

/**
 * Created by StormSpirit on 12/3/2016.
 */
public class Server {
    public static void main(String[] args) throws IOException {
        try{

            InetAddress ip = InetAddress.getLocalHost();
            System.out.println("IP Broker: " + ip.getHostAddress());

            ServerSocket serverSocket = new ServerSocket(1900,50, ip);
         //   ServerSocket serverSocket = new ServerSocket(1900);
            System.out.println("Server is listenning at port : 1900");
            System.out.println("Waiting connection from client...");

            while (true){
                Socket socket = serverSocket.accept();
                System.out.println("Connecting: " + socket.getInetAddress().getHostAddress());
                new Listener(socket).start();
            }

        }catch(Exception e){
            System.out.println("Error at First: " + e.getMessage());
        }
    }
}

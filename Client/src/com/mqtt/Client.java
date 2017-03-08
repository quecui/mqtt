package com.mqtt;

import com.mqtt.controller.AutoListenner;
import com.mqtt.model.Channel;
import com.mqtt.model.Connect;
import com.mqtt.services.Services;
import com.mqtt.ui.UI;

import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

/**
 * GUIDE
 * Resources:
 * + client.txt - Save info about client : username - clientID - nameChanel - listfileUploaded
 */

public class Client {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("=========== Input Broker Infomation =========\n\n");
        Scanner scanner = new Scanner(System.in);
       String address = "";
        while (true){
            System.out.print("Input host Address: ");
            address = scanner.nextLine();
            if (new Client().checkExistAddress(address)){
               break;
            }else {
                System.out.println("Address Wrong! Please try again");
                continue;
            }
        }
        Socket socket = new Socket(address, 1900);

        Services services = new Services(socket);
        services.createResources();

        UI ui = new UI(socket);
        ui.createUser();

        Connect connect = services.createConnection();
        if (connect == null)
            return;
        //todo: show list channels on broker -> done
        List<Channel> channels = services.getChanelListOnBroker();
        ui.createBanner();
        boolean check = services.showChanelListOnBroker(channels);
        //todo: create SocketServer
        AutoListenner autoListenner = new AutoListenner(socket, connect.getClientID());
        autoListenner.setName("autoListener");
        autoListenner.start();
        //todo: send list requried file
        services.sendRequiredChannel(channels);

        ui.createMenu(channels, check);
    }

    public boolean checkExistAddress(String address){
        try(Socket socket = new Socket(address, 1900)){
            return true;
        }catch (Exception e){
        }
        return false;
    }
}

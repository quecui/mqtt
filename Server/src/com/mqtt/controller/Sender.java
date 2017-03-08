package com.mqtt.controller;

import com.mqtt.model.Publish;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Set;

/**
 * Created by StormSpirit on 11/25/2016.
 */
public class Sender extends Thread {

    Publish publish;
    int clientID;
    boolean status;
    String IPDes;

    public Sender(Publish publish, int clientID, boolean status, String IPDes){
        this.publish = publish;
        this.clientID = clientID;
        this.status = status;
        this.IPDes = IPDes;
    }

    public void run(){
        try {
            if(status)
                publish.setPacketID(clientID);

            ObjectMapper mapper = new ObjectMapper();
            String jsonMessage = mapper.writeValueAsString(publish);
            String fileName = "";
            if(status)
                fileName = "resources/" + clientID + "/" + publish.getNameChanel().split("/")[1];
            else
                fileName = "resources/" + publish.getPacketID() + "/" + publish.getNameChanel().split("/")[1];
            Socket socket = new Socket(IPDes, (clientID + 2000));
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bw.write(jsonMessage);
            bw.newLine();
            bw.flush();

            Thread.sleep(1000);
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
            FileInputStream fis = new FileInputStream(fileName);
            byte[] buffer = new byte[1024];

            foo:
            while (true){
                while(fis.read(buffer) > 0){
                    dos.write(buffer);
                }
                dos.write(new byte[1]);

                String check = br.readLine();
                if(check.equals("0")){
                    System.out.println("Send file error");
                    continue foo;
                }else {
                    System.out.println("Send file success");
                    br.close();
                    dos.close();
                    fis.close();
                    break foo;
                }
            }
        }catch (Exception e){
            System.out.println("Error at Sender" + e.getMessage());
        }
    }
}

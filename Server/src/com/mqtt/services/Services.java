package com.mqtt.services;

import com.mqtt.controller.Sender;
import com.mqtt.model.*;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by StormSpirit on 12/3/2016.
 */
public class Services {
    Socket socket;
    ObjectMapper mapper;
    BufferedReader bufferedReader;
    BufferedWriter bufferedWriter;
    int clientID;
    String username;

    public Services(Socket socket) throws IOException {
        this.socket = socket;
        this.mapper = new ObjectMapper();
        this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    public boolean createResources() throws IOException {
        File file = new File("resources");
        if(file.exists()){
            return true;
        }

        file.mkdir();
        File file1 = new File("resources/channelList.txt");
        File file2 = new File("resources/currentID.txt");
        File file3 = new File("resources/subcribeList.txt");
        BufferedWriter bw = new BufferedWriter(new FileWriter(file1));
        BufferedWriter bw1 = new BufferedWriter(new FileWriter(file2));
        BufferedWriter bw2 = new BufferedWriter(new FileWriter(file3));

        bw1.write("0");
        bw2.write("0/0/0\n");

        bw.close();
        bw1.close();
        bw2.close();
        return false;
    }

    //Example packet: {"type":1,"clientID":21,"cleanSession":false,"willMessage":"Offline","keepAlive":60}
    public String getTypeMessage(String jsonMessage) {
        int start = 0, end = 0;

        foo:
        for (int i = 0; i < jsonMessage.length(); i++) {
            if (jsonMessage.charAt(i) == ':') {
                start = i + 1;
                for (int j = i + 1; j < jsonMessage.length(); j++) {
                    if (jsonMessage.charAt(j) == ',') {
                        end = j;
                        break foo;
                    }
                }
            }
        }

        return jsonMessage.substring(start, end);
    }

    private List<String> readFile(String fileName) throws IOException {
        File file = new File(fileName);
        BufferedReader br = new BufferedReader(new FileReader(file));

        List<String> list = new ArrayList<String>();
        String inLine = "";

        while ((inLine = br.readLine()) != null) {
            list.add(inLine);
        }

        return list;
    }

    private boolean writeFile(String fileName, boolean status, String value) throws IOException {
        File file = new File(fileName);
        FileWriter fw = new FileWriter(file, status);

        fw.write(value + "\n");
        fw.close();
        return true;
    }

    //======================================= CONNECT ========================================
    public Connect processConnectMessage(String jsonMessage) throws IOException {
        Connect connectMessage = mapper.readValue(jsonMessage, Connect.class);
        boolean result = checkClientID(connectMessage.getClientID());

        if (result) {
            saveClientID(connectMessage.getClientID());
            System.out.println("Welcome: " + socket.getInetAddress());
            clientID = connectMessage.getClientID();
            username = connectMessage.getUsername();
            sendConnectAck(result);
            return connectMessage;
        } else {
            System.out.println("Deny access from: " + socket.getInetAddress());
            sendConnectAck(result);
            return null;
        }
    }

    public boolean checkClientID(int clientID) throws IOException {
        /*
            todo: Code check Client ID --> done
         */
        List<String> listClientID = readFile("resources/currentID.txt");
        String[] clientIDs = listClientID.get(0).split("-");

        for (String str : clientIDs) {
            if (str.equals(String.valueOf(clientID))) {
                return false; // Exist ClientID
            }
        }
        return true;
    }

    public boolean saveClientID(int clientID) throws IOException {
        List<String> listClientID = readFile("resources/currentID.txt");
        String list = listClientID.get(0);

        new FileWriter(new File("resources/currentID.txt"));
        list += "-" + clientID;
        writeFile("resources/currentID.txt", true, list);

        listClientID.remove(0);
        listClientID.add(list);

        return true;
    }

    private boolean sendConnectAck(boolean result) throws IOException {
        ConnectACK connectACK = new ConnectACK();

        if (result == false) {
            connectACK.setReturnCode(1);
        } else {
            connectACK.setReturnCode(0);
        }

        String jsonMessage = mapper.writeValueAsString(connectACK);

        bufferedWriter.write(jsonMessage);
        bufferedWriter.newLine();
        bufferedWriter.flush();

        if (result == false) {
            System.out.println("Connect from : " + socket.getInetAddress() + " closed");
            bufferedWriter.close();
            return false;
        }

        return true;
    }

    public boolean sendListChannelToClient() throws IOException {
        List<String> chanelString = readFile("resources/channelList.txt");
        List<Channel> channels = new ArrayList<Channel>();

        for (String str : chanelString) {
            Channel channel = mapper.readValue(str, Channel.class);
            channels.add(channel);
        }

        ChannelList channelList = new ChannelList();
        channelList.setChannels(channels);
        String jsonMessage = mapper.writeValueAsString(channelList);
        bufferedWriter.write(jsonMessage);
        bufferedWriter.newLine();
        bufferedWriter.flush();
        return true;
    }

    //======================== PUBLISH ==================================
    public Publish processPublishMessage(String jsonMessage) throws IOException, InterruptedException {
        Publish publish = mapper.readValue(jsonMessage, Publish.class);
        //todo: Save topic name to databases->done

        boolean result = saveChannel(publish);
        //todo: Filter Subcribe ?? - not done -------------------------------------------------------------------------------------------
        if (result) {//chanel exist -> send publish to all subcribe
            List<String> subcribeList = readFile("resources/subcribeList.txt");

            foo:
            for(String str:subcribeList){
                List<String> IDList = Arrays.asList(str.split("/"));
                for(int i = 1; i < IDList.size(); i++){
                    if(IDList.get(i).equals(String.valueOf(clientID))){
                        new Sender(publish, Integer.parseInt(IDList.get(0)), true, socket.getInetAddress().getHostAddress()).start();
                        continue foo;
                    }
                }
            }
        }

        PublishACK publishACK = new PublishACK();
        publishACK.setPacketID(publish.getPacketID());

        jsonMessage = mapper.writeValueAsString(publishACK);
        System.out.println("ConnectACK: " + jsonMessage);

        bufferedWriter.write(jsonMessage);
        bufferedWriter.newLine();
        bufferedWriter.flush();

        processUploadFromClient(Long.parseLong(publish.getPayload()), "resources/" + clientID + "/" + publish.getNameChanel().split("/")[1]);
        return publish;
    }

    public boolean processUploadFromClient(long fileSize, String fileName) throws IOException {
        DataInputStream dis = new DataInputStream(socket.getInputStream());

        File file = new File(fileName);
        file.getParentFile().mkdir();
        FileOutputStream fos = new FileOutputStream(fileName);
        byte[] buffer = new byte[1024];

        while (true) {
            int lengthPacket = 0;
            int countByteRecv = 0;
            while ((lengthPacket = dis.read(buffer, 0, 1024)) > 0) {
                if (lengthPacket <= 1)
                    break;
                countByteRecv += lengthPacket;
                fos.write(buffer, 0, lengthPacket);
            }

            //todo : convert byte -> MB
            fileSize = (int)file.length();
            if (countByteRecv < fileSize) {
                System.out.println("Wrong about fileSize. Upload fail");
                bufferedWriter.write("0");
                bufferedWriter.newLine();
                bufferedWriter.flush();
                continue;

            } else {
                System.out.println("Upload Success");
                bufferedWriter.write("1");
                bufferedWriter.newLine();
                bufferedWriter.flush();
                return true;
            }
        }

    }
    public boolean saveChannel(Publish publish) throws IOException {
        //todo: check exist channel -> get All channel by Id -> done
        List<String> chanelList = checkExistChannel(clientID);

        //fasle
        List<String> fileList = new ArrayList<String>();
        String fileName = publish.getNameChanel();
        String nameChanel = fileName.split("/")[0];
        fileName = fileName.split("/")[1];

        fileList.add(fileName); //todo: in order to add new Channel to ChannelList

        if (chanelList != null) {
            Channel channel = new Channel();
            for (String str : chanelList) {
                channel = mapper.readValue(str, Channel.class);
                if (channel.getClientID() == clientID) {
                    chanelList.remove(str);
                    channel.getListfile().add(fileName);
                    String tmp = mapper.writeValueAsString(channel);
                    chanelList.add(tmp);

                    new FileWriter(new File("resources/channelList.txt"));
                    for (String string : chanelList) {
                        writeFile("resources/channelList.txt", true, string);
                    }
                    File file = new File("resources/" + clientID);
                    file.mkdir();

                    return true;
                }
            }
        }

        // == NULL
        Channel channel = new Channel();
        channel.setClientID(clientID);
        channel.setNameChanel(nameChanel);
        channel.setListfile(fileList);
        String jsonMessage = mapper.writeValueAsString(channel);

        writeFile("resources/channelList.txt", true, jsonMessage);
        return false;
    }

    public List<String> checkExistChannel(int clientID) throws IOException {
        List<String> chanelList = readFile("resources/channelList.txt");
        Channel channel = new Channel();

        for (String str : chanelList) {
            channel = mapper.readValue(str, Channel.class);
            if (channel.getClientID() == clientID) {
                return chanelList;
            }
        }

        return null;
    }

    //=========================================== SUBCRIBE =======================================
    public boolean processSubcribeMessage(String jsonMessage) throws IOException {
        Subcribe subcribe = mapper.readValue(jsonMessage, Subcribe.class);
        int topicID = subcribe.getTopicID();
        List<String> chanelList = checkExistChannel(topicID);//todo: not use??????????????

        //todo: save list subcribe ->done
        saveToSubcribeList(clientID, topicID);
        SubcribeACK subcribeACK = new SubcribeACK();
        subcribeACK.setPacketID(subcribe.getTopicID());
        jsonMessage = mapper.writeValueAsString(subcribeACK);

        bufferedWriter.write(jsonMessage);
        bufferedWriter.newLine();
        bufferedWriter.flush();

        //todo: get file from client:
        return true;
    }

    public boolean saveToSubcribeList(int clientID, int topicID) throws IOException {
        List<String> subcribeList = readFile("resources/subcribeList.txt");
        subcribeList.remove(0);
        writeFile("resources/subcribeList.txt", false, "0/0/0");

        //Format: subcribeID/hostID/hostID/hostID
        for (String str : subcribeList) {
            String tmp = str.split("/")[0];
            int id = Integer.parseInt(tmp);
            if (id == clientID) {
                subcribeList.remove(str);
                str += "/" + topicID;
                subcribeList.add(str);

                for (String string : subcribeList) {
                    writeFile("resources/subcribeList.txt", true, string);
                }

                return true;
            }
        }

        String str = clientID + "/" + topicID;
        subcribeList.add(str);

        for (String string : subcribeList) {
            writeFile("resources/subcribeList.txt", true, string);
        }
        return true;
    }

    //====================================== UNSUBCIRBE ================================
    public boolean processUnsubcribeMessage(String jsonMessage) throws IOException {
        System.out.println("UNSUB: " + jsonMessage + "\n\n\n\n\n\n");


        Unsubcribe unsubcribe = mapper.readValue(jsonMessage, Unsubcribe.class);
        //todo: Delete subcribe from list -> done
        deleteSubcribeTopic(clientID, unsubcribe.getPacketID());//delete topicID
        UnsubACK unsubACK = new UnsubACK();
        unsubACK.setPacketID(unsubcribe.getPacketID());
        jsonMessage = mapper.writeValueAsString(unsubACK);

        bufferedWriter.write(jsonMessage);
        bufferedWriter.newLine();
        bufferedWriter.flush();
        System.out.println("UNSUB: " + jsonMessage + "\n\n\n\n\n\n");
        return true;
    }

    public boolean deleteSubcribeTopic(int clientID, int topicID) throws IOException {
        List<String> subcribeList = readFile("resources/subcribeList.txt");
        subcribeList.remove(0);
        writeFile("resources/subcribeList.txt", false, "0/0/0/0");

        String inLine = "";
        for (String str : subcribeList) {
            String[] tmp = str.split("/");
            int id = Integer.parseInt(tmp[0]);

            if (id == clientID) {
                inLine += id;
                subcribeList.remove(str);
                boolean check = false;
                for (int i = 1; i < tmp.length; i++) {
                    if (Integer.parseInt(tmp[i]) == topicID && check == false) {
                        tmp[i] = "0";
                        check = true;
                    }
                    inLine += "/" + tmp[i];
                }
                subcribeList.add(inLine);

                for (String ele : subcribeList) {
                    writeFile("resources/subcribeList.txt", true, ele);
                }

                return true;
            }
        }

        return true;
    }

    public boolean deleteClientID(int clientID) throws IOException {
        List<String> clientList = readFile("resources/currentID.txt");
        List<String> array = new ArrayList<String>(Arrays.asList(clientList.get(0).split("-")));
        array.remove(0);

        File file = new File("resources/currentID.txt");
        FileWriter fw = new FileWriter(file);
        fw.close();

        String inLine = "0";
        for(String str:array){
            if(str.equals(String.valueOf(clientID))){
                clientList.remove(str);
                continue;
            }
            inLine += "-" + str;
        }

        writeFile("resources/currentID.txt", true, inLine);
        return true;
    }

    public boolean getRequiredChannel(String jsonMessage) throws IOException, InterruptedException {
        Publish publish = new Publish();//send all subcriber

        ChannelList channelList = mapper.readValue(jsonMessage, ChannelList.class);
        List<Channel> channels = channelList.getChannels();

        for (Channel channel : channels) {
            for (String str : channel.getListfile()) {
                String topicName = channel.getNameChanel() + "/" + str;
                publish.setPacketID(channel.getClientID());
                publish.setNameChanel(topicName);
                publish.setMessage("New file Downloaded To Your Device!");
                publish.setPayload(getFileSize(str, channel.getClientID()));

                new Sender(publish, clientID, false, socket.getInetAddress().getHostAddress()).start();
                Thread.sleep(2000);
            }
        }

        return true;
    }

    public String getFileSize(String fileName, int clientID){
        File file = new File("resources/" + clientID + "/" + fileName);
        long fileSize = file.length();
        return String.valueOf(fileSize);
    }


}

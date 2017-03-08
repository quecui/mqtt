package com.mqtt.services;

import com.mqtt.controller.Timer;
import com.mqtt.model.*;
import com.mqtt.ui.UI;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by StormSpirit on 12/3/2016.
 */
public class Services {
    ObjectMapper mapper;
    Socket socket;
    BufferedReader bufferedReader;
    BufferedWriter bufferedWriter;
    String pathFile;
    String fileName;
    String nameOfYourChanel;
    int clientID;

    public Services(Socket socket) throws IOException {
        this.socket = socket;
        this.mapper = new ObjectMapper();
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
    }

    // =========================== FUNCTION SUPPORT =============================
    public boolean createResources() throws IOException {
        File file = new File("resources");
        if(file.exists()){
            return true;
        }

        file.mkdir();
        File file1 = new File("resources/client.txt");
        File file2 = new File("resources/listFile.txt");
        BufferedWriter bw = new BufferedWriter(new FileWriter(file1));
        BufferedWriter bw1 = new BufferedWriter(new FileWriter(file2));

        bw.write("username:\nclientID:\nnameChanel:\nlistfileUploaded:");
        bw1.write("0/0/0/0\n");
        bw.close();
        bw1.close();

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

    public boolean writeFile(String fileName, boolean status, String value) throws IOException {
        File file = new File(fileName);
        FileWriter fw = new FileWriter(file, status);

        fw.write(value + "\n");
        fw.close();
        return true;
    }

    public List<String> readFile(String fileName) throws IOException {
        File file = new File(fileName);
        BufferedReader br = new BufferedReader(new FileReader(file));

        List<String> list = new ArrayList<String>();
        String inLine = "";

        while ((inLine = br.readLine()) != null) {
            list.add(inLine);
        }

        return list;
    }

    //============================== CONNECT =================================
    public Connect createConnection() throws IOException {
        Connect connectMessage = createConnectMessage(); // create Connect message

        String jsonMessage = mapper.writeValueAsString(connectMessage); // parse object Connect to Json
        bufferedWriter.write(jsonMessage);
        bufferedWriter.newLine();
        bufferedWriter.flush();
        while (true) {
            while ((jsonMessage = bufferedReader.readLine()) == null) ; //wait connect ACK from server
            if(getTypeMessage(jsonMessage).equals("2"))
                break;
        }
        ConnectACK connectACK = mapper.readValue(jsonMessage, ConnectACK.class);

        if (connectACK.getReturnCode() == 0) {
            System.out.println("Create Connection Success!");
            clientID = connectMessage.getClientID();
            saveClientIDToFile(String.valueOf(connectMessage.getClientID()));
            return connectMessage;
        } else {
            System.out.println("Create Connection Fail! \nPlease try again");
            socket.close();
            return null;
        }
    }

    public Connect createConnectMessage() throws IOException {
        Connect connect = new Connect();
        File file = new File("resources/client.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String username = br.readLine();
        username = username.split(":")[1];
        connect.setUsername(username);

        String clientID = br.readLine();
        if (clientID.equals("clientID:")) {//don't exist clientID
            Random rd = new Random();
            connect.setClientID(rd.nextInt(100));
        } else {// exist clientID
            clientID = clientID.split(":")[1]; // read clientID from file
            connect.setClientID(Integer.parseInt(clientID));
        }

        return connect;
    }

    public boolean saveClientIDToFile(String clientID) throws IOException {
        List<String> clientInfo = readFile("resources/client.txt");
        if (!clientInfo.get(1).equals("clientID:"))
            return false;

        clientID = clientInfo.get(1) + clientID;
        clientInfo.set(1, clientID);

        BufferedWriter bw = new BufferedWriter(new FileWriter(new File("resources/client.txt")));//clear file
        bw.close();

        for (String str : clientInfo) {
            writeFile("resources/client.txt", true, str);
        }

        return true;
    }

    // ========================================= GET CHANELS ON BROKER ===================================
    public List<Channel> getChanelListOnBroker() throws IOException {
        String jsonMessage = null;
        while ((jsonMessage = bufferedReader.readLine()) == null);
        ChannelList channelList = mapper.readValue(jsonMessage, ChannelList.class);
        List<Channel> channels = channelList.getChannels();

        return channels;
    }

    public boolean sendRequiredChannels() throws IOException {
        ChannelList channelList = new ChannelList();
        channelList.setType(11);
        String jsonMessage = mapper.writeValueAsString(channelList);

        bufferedWriter.write(jsonMessage);
        bufferedWriter.newLine();
        bufferedWriter.flush();

        return true;
    }

    public boolean showChanelListOnBroker(List<Channel> channels) throws IOException {
        List<Channel> chanels1 = new ArrayList<Channel>(channels);
        List<String> list = readFile("resources/listFile.txt");
        List<String> clientInfo = readFile("resources/client.txt");
        clientID = Integer.parseInt(clientInfo.get(1).split(":")[1]);
        list.remove(0);

        for (Channel channel : chanels1) {
            if (channel.getClientID() == clientID) {
                chanels1.remove(channel);
                break;
            }
        }

        foo:
        for (String str : list) {
            String ID = str.split("/")[0];
            for (int i = 0; i < chanels1.size(); i++) {//Channel chanel:chanels1
                if ((chanels1.get(i).getClientID() == Integer.parseInt(ID))) {
                    chanels1.remove(i);
                    i--;

                    if (chanels1.size() == 0)
                        break foo;
                }

            }
        }

        if (chanels1.size() <= 0) {
            System.out.println("================== No New Channel On Broker ==================");
            return false;
        }
        System.out.println("================== List New Chanels on Broker ====================");
        for (int i = 0; i < chanels1.size(); i++) {
            System.out.println((i + 1) + "." + chanels1.get(i).getNameChanel());//+ "- by " + channels.get(i).getClientID()
        }

        return true;
    }

    public boolean sendRequiredChannel(List<Channel> channels) throws IOException {//chanels: List channels from Broker
        List<Channel> chanels1 = new ArrayList<Channel>(); // Get channels matched between client and broker
        List<String> currentChanelList = readFile("resources/listFile.txt"); //get All file by channels
        ChannelList channelList = new ChannelList();
        currentChanelList.remove(0); // remove null value

        if (currentChanelList.size() == 0) {
            channelList.setType(0); // don't required
        }

        if (currentChanelList.size() != 0) { //required

            foo:
            for (String str : currentChanelList) {
                List<String> tmp = new ArrayList<String>(Arrays.asList(str.split("/"))); // get dowloaded files in a channel in client

                //Format: clientID/file/file/file
                for (Channel channel : channels) { // get channel on broker
                    if (String.valueOf(channel.getClientID()).equals(tmp.get(0))) {
                        chanels1.add(channel); //  match channel in client with channel on broker by clientID
                        break;
                    }
                }

                tmp.remove(tmp.get(0)); //remove clientID to find file need to download.


                Channel channel = chanels1.get(chanels1.size() - 1); //get new added channel.
                chanels1.remove(channel); // remove to fix
                List<String> fileList = channel.getListfile();
                for (String file : tmp) {//find a new file on broker in a channel
                    fileList.remove(file);
                }
                channel.setListfile(fileList);
                chanels1.add(channel); // add channel again.

            }

            channelList.setChannels(chanels1);//get All channels required
        }

        String jsonMessage = mapper.writeValueAsString(channelList);
        bufferedWriter.write(jsonMessage);
        bufferedWriter.newLine();
        bufferedWriter.flush();

        return true;
    }

    //========================================== PUBLISH =================================================
    public Publish processPublishReturn(String jsonMessage) throws IOException {
        Publish publish = mapper.readValue(jsonMessage, Publish.class);
        String fileName = publish.getNameChanel();
        fileName = fileName.split("/")[1];

        List<String> listFile = readFile("resources/listFile.txt");
        listFile.remove(0);

        if (listFile.size() == 0) {
            listFile.add(publish.getPacketID() + "/" + fileName);
        } else {
            for (String str : listFile) {
                String strTmp = str;
                String[] tmp = str.split("/");
                if (tmp[0].equals(String.valueOf(publish.getPacketID()))) {
                    listFile.remove(str);

                    strTmp += "/" + fileName;
                    listFile.add(strTmp);
                }
            }
        }

        writeFile("resources/listFile.txt", false, "0/0/0/0");
        for (String str : listFile) {
            writeFile("resources/listFile.txt", true, str);
        }
        //System.out.println("Save thanh cong");
        return publish;
    }

    public boolean sendPublishMessage() throws IOException {
        Publish publish = createPublishMessage();
        String jsonMessage = mapper.writeValueAsString(publish);

        //QoS = 1
        qos:
        while (true) {
            bufferedWriter.write(jsonMessage);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Random rd = new Random(); // don't duplicate name 2 thread Timer
            Timer timer = new Timer();
            String nameThread = "timer" + String.valueOf(rd.nextInt(100));
            timer.setName(nameThread);
            timer.start();

            bufferedReader.ready();
            while (true) {
                while ((jsonMessage = bufferedReader.readLine()) == null) {
                    if (!timer.checkThreadAlive(nameThread)) { // check Timeout
                        jsonMessage = mapper.writeValueAsString(publish);
                        continue qos;
                    }
                }
                if (getTypeMessage(jsonMessage).equals("4"))
                    break ;
            }
            //todo: convert to object -> check valid object???
            //todo: Create new thread and send data
            if (jsonMessage != null) {
                System.out.println("Send Publish Message Success!");

                return true;
            } else {
                System.out.println("Send Publish Message Fail!");
                return false;
            }
        }

    }

    public Publish createPublishMessage() throws IOException {
        Publish publish = new Publish();
        String nameChanel = new UI(socket).checkExistChanel();
        Scanner input = new Scanner(System.in);

        if (nameChanel.equals("null")) {
            System.out.println("================= Create New Channel ================");
            System.out.print("Enter your nameChanel: ");
            nameChanel = input.nextLine();
        }

        nameOfYourChanel = nameChanel;
        String message;
        System.out.print("Enter your message: ");
        message = input.nextLine();
        while (true) {
            System.out.print("Enter pathFile: ");
            pathFile = input.nextLine();
            fileName = getNameFile(pathFile);
            if(!checkMP3(fileName)){
                System.out.println("Please input MP3 file");
                continue;
            }

            File file = new File(pathFile);
            if(!file.exists()){
                System.out.println("File doesn't exist!");
                continue;
            }
            nameChanel += "/" + fileName;

            publish.setPayload(String.valueOf(file.length()));
            publish.setMessage(message);
            publish.setNameChanel(nameChanel);

            return publish;
        }
    }

    public String getNameFile(String pathFile) {
        Path path = Paths.get(pathFile);
        return path.getFileName().toString();
    }

    public boolean checkMP3(String fileName) {
       fileName = fileName.substring(fileName.length() - 3,fileName.length());
        if (fileName.equals("mp3"))
            return true;
        else
            return false;
    }

    //default : fileName same link/fileName
    public boolean uploadFile() throws IOException {
        System.out.println("Wait! Your File is uploading.....");
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        FileInputStream fis = new FileInputStream(pathFile);
        byte[] buffer = new byte[1024];

        foo:
        while (true) {
            while (fis.read(buffer) > 0) {
                dos.write(buffer);
            }
            dos.write(new byte[1]);

            String check = bufferedReader.readLine();
            if (check.equals("0")) {
                System.out.println("Send file error");
                continue foo;
            } else {
                System.out.println("Send file success");
                fis.close();

                addUploadedfile(fileName, nameOfYourChanel);
                break foo;
            }
        }

        return true;
    }

    public boolean addUploadedfile(String fileName, String nameChanel) throws IOException {
        List<String> clientInfo = readFile("resources/client.txt");

        String fileList = clientInfo.get(3);
        clientInfo.remove(fileList);// remove to add new

        String name = clientInfo.get(2);
        if (name.equals("nameChanel:")) {
            clientInfo.remove(2);
            name += nameChanel;
            clientInfo.add(name);
        }

        if (fileList.equals("listfileUploaded:"))//listFileUploaded
            fileList += fileName;
        else
            fileList += "/" + fileName;
        clientInfo.add(fileList);

        BufferedWriter bw = new BufferedWriter(new FileWriter(new File("resources/client.txt")));//clear data in file
        bw.close();

        for (String str : clientInfo) {
            writeFile("resources/client.txt", true, str);
        }

        return true;
    }

    public void showYourfiles() throws IOException {
        List<String> clientInfo = readFile("resources/client.txt");
        List<String> fileList = Arrays.asList(clientInfo.get(3).split(":")[1].split("/"));

        System.out.println("Your Channel Name: " + clientInfo.get(2).split(":")[1]);
        System.out.println("file in your Channel");
        for (int i = 1; i <= fileList.size(); i++) {
            System.out.println((i) + ". " + fileList.get(i - 1));
        }
    }

    //===================================== SUBCRIBER ============================================
    public boolean sendSubcribeMessage(List<Channel> channels, boolean check) throws IOException {
        Subcribe subcribe = createSubcribeMessage(channels, check);
        if (subcribe == null)
            return false;
        String jsonMessage = mapper.writeValueAsString(subcribe);

        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        bw.write(jsonMessage);
        bw.newLine();
        bw.flush();

        BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        while (true){
            while ((jsonMessage = br.readLine()) == null) ;
            if(getTypeMessage(jsonMessage).equals("6"))
                break;
        }
        if (jsonMessage != null) {
            System.out.println("Send Subcribe Message Success!");

            //save chanel subcried
            List<String> listFile = readFile("resources/listFile.txt");
            listFile.remove(0);
            listFile.add(String.valueOf(subcribe.getTopicID()));
            writeFile("resources/listFile.txt", false, "0/0/0/0");
            for (String str : listFile) {
                writeFile("resources/listFile.txt", true, str);
            }

            return true;
        } else {
            System.out.println("Send Subcribe Message Fail!");
            return false;
        }

    }

    public Subcribe createSubcribeMessage(List<Channel> channels, boolean check) throws IOException {
        List<Channel> chanels1 = new ArrayList<>(channels);
        List<String> clientInfo = readFile("resources/client.txt");
        clientID = Integer.parseInt(clientInfo.get(1).split(":")[1]);
        Scanner input = new Scanner(System.in);

        Subcribe subcribe = new Subcribe();
        int key = 0;

        List<String> list = readFile("resources/listFile.txt");
        list.remove(0);

        if (check == false) {
            System.out.println("You subcribed all channels!");
            return null;
        }

        for (Channel channel : chanels1) {
            if (channel.getClientID() == clientID) {
                chanels1.remove(channel);
                break;
            }
        }

        int size = chanels1.size();
        for (String str : list) {
            String ID = str.split("/")[0];
            for (int i = 0; i < chanels1.size(); i++) {
                if (chanels1.get(i).getClientID() == Integer.parseInt(ID)) {
                    chanels1.remove(i);
                    size--;
                }

            }
        }

        if (size <= 0) {
            System.out.println("You subcribed all channels!");
            return null;
        }

        while (true) {
            System.out.print("Enter key of chanel: ");
            key = input.nextInt();
            if (key <= chanels1.size()) {
                subcribe.setTopicID(chanels1.get(key - 1).getClientID());
                break;
            } else
                System.out.println("Invalid key! Please again");
        }

        return subcribe;
    }

    //=============================== UNSUBCRIBE =============================
    public boolean showSubcribedChanels(List<Channel> channels) throws IOException {
        List<String> listChanel = readFile("resources/listFile.txt");
        listChanel.remove(0);
        List<String> chanelInfo = new ArrayList<String>();

        for (String str : listChanel) {
            String ID = str.split("/")[0];

            for (Channel channel : channels) {
                if (channel.getClientID() == Integer.parseInt(ID)) {
                    chanelInfo.add(channel.getNameChanel());
                }
            }
        }

        if (chanelInfo.size() == 0) {
            System.out.println("You don't subcribe any chanel!");
            return false;
        } else {
            System.out.println("You subcribed channels");
            for (int i = 1; i <= chanelInfo.size(); i++) {
                System.out.println(i + ". " + chanelInfo.get(i - 1));
            }
        }

        return true;
    }

    public boolean sendUnsubcribeMessage(int key) throws IOException {
        List<String> listChanel = readFile("resources/listFile.txt");
        Unsubcribe unsubcribe = createUnsubcribeMessage(Integer.parseInt(listChanel.get(key).split("/")[0]));

        String jsonMessage = mapper.writeValueAsString(unsubcribe);

        bufferedWriter.write(jsonMessage);
        bufferedWriter.newLine();
        bufferedWriter.flush();

        bufferedReader.ready();
        while (true) {
            while ((jsonMessage = bufferedReader.readLine()) == null) ;
            if(getTypeMessage(jsonMessage).equals("8"))
                break;
        }
        if (jsonMessage != null) {

            System.out.println("Send Unsubcribe Message Success!");

            //delete Infomation about chanel
            listChanel.remove(key);
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File("resources/listFile.txt")));
            bw.close();

            for (String str : listChanel) {
                writeFile("resources/listFile.txt", true, str);
            }
            return true;
        } else {
            System.out.println("Send Unsubcribe Message Fail!");
            return false;
        }
    }

    public Unsubcribe createUnsubcribeMessage(int clientID) {
        Unsubcribe unsubcribe = new Unsubcribe();
        unsubcribe.setPacketID(clientID);

        return unsubcribe;
    }

    //=========================================== Auto Get Data ========================================
    public boolean getData(Socket socket, String jsonMessage) throws IOException {//int clientID, String name,
        Publish publish = mapper.readValue(jsonMessage, Publish.class);
        int clientID = publish.getPacketID();
        String name = publish.getNameChanel().split("/")[1];
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        String fileName = "resources/" + clientID + "/" + name;

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
            int fileSize = (int) file.length();
            if (countByteRecv < fileSize) {
                System.out.println("Wrong about fileSize. Upload fail");
                bw.write("0");
                bw.newLine();
                bw.flush();
                continue;

            } else {
                bw.write("1");
                bw.newLine();
                bw.flush();
                //System.out.println("Download thanh cong");
                return true;
            }
        }
    }


    public List<String> showSongs(int key) throws IOException {
        List<String> files = readFile("resources/listFile.txt");
        files.remove(0);
        String listMusic = files.get(key - 1);
        String[] array = listMusic.split("/");

        System.out.println("List Songs");
        for (int i = 1; i < array.length; i++) {
            System.out.println(i + ". " + array[i]);
        }

        return Arrays.asList(array);
    }

}

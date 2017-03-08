package com.mqtt.ui;

import com.mqtt.model.Channel;
import com.mqtt.services.Services;
import jaco.mp3.player.MP3Player;

import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

/**
 * Created by StormSpirit on 12/3/2016.
 */
public class UI {
    String os;
    Socket socket;
    Services services;
    public UI(Socket socket) throws IOException {
        this.socket = socket;
        this.os = System.getProperty("os.name");
        this.services = new Services(socket);
    }

    //Get clientID and username from client.txt
    public void createBanner() throws IOException {
        clearConsole();
        File file = new File("resources/client.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String username = br.readLine();
        String clientID = br.readLine();
        br.close();

        clientID = clientID.split(":")[1];
        username = username.split(":")[1];
        System.out.println("===================== CLIENT =====================");
        System.out.println("ClientID: " + clientID + "  |  Username: " + username);
    }

    public void clearConsole(){
        try {
            if (os.contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else {
                char ESC = 27;
                Console c = System.console();
                c.writer().print(ESC + "[2J");
                c.flush();
            }
        } catch (final Exception e) {
            System.out.println("****** Error at Clear Console ******");
        }
    }

    public void createUser() throws IOException, InterruptedException {
        clearConsole();
        if(checkExistUser())
            return;

        Scanner input = new Scanner(System.in);
        System.out.print("Enter Your Name: ");
        String username = input.nextLine();
        saveUser(username);
        clearConsole();
    }

    public void saveUser(String username) throws IOException {
        File file = new File("resources/client.txt");
        BufferedWriter bw = new BufferedWriter(new FileWriter(file));

        String tmp = "username:" + username;
        bw.write(tmp + "\n");
        bw.write("clientID:\n" + "nameChanel:\n" + "listfileUploaded:");
        bw.close();
    }

    private boolean checkExistUser() throws IOException {
        File file = new File("resources/client.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));
        String username = br.readLine();

        if(username.equals("username:")){
            return false; // don't exist
        }
        return true; // exist
    }

    public void createMenu(List<Channel> channels, boolean check) throws IOException, InterruptedException {
        String existChanel = checkExistChanel();

        Scanner input = new Scanner(System.in);
        System.out.println("======= MENU =======");

        if(!existChanel.equals("null")){
            System.out.println("1. Your Channel");
        }else {
            System.out.println("1. Create new Channel");
        }

        System.out.println("2. Subcribe Channel");
        System.out.println("3. Unsubcribe");
        System.out.println("4. Listen to music");
        System.out.print("\nEnter your key: ");
        String key = input.nextLine();

        switch (key){
            case "1":
                if(!existChanel.equals("null")){
                    clearConsole();
                    yourChanel(socket);
                }else { // create New Channel
                    clearConsole();
                    services.sendPublishMessage();
                    services.uploadFile();
                    System.out.println("Your Channel Has Created.");
                }

                break;
            case "2":
                //todo: Subcribe chanel
                services.sendSubcribeMessage(channels, check);
                services.sendRequiredChannel(channels);
                break;
            case "3":
                //todo : show list chanel subcried
                unSubcribeMenu(socket, channels);
                // todo: send unsub

                break;
            case "4":
                //todo: show list chanel
                //todo: show file in chanel
                //todo:play music
                musicMenu(socket, channels);

                break;
            default:
                System.out.println("Key Invalid");
                break;
        }

        System.out.println("Note: Press x to back Home Menu");
        String press = input.nextLine();
        if(press.equals("x")){
            clearConsole();
            createBanner();

            services.sendRequiredChannels();
            channels = services.getChanelListOnBroker();
            check = services.showChanelListOnBroker(channels);
            createMenu(channels, check);
        }

    }

    public void musicMenu(Socket socket, List<Channel> channels) throws IOException, InterruptedException {
        Scanner scanner = new Scanner(System.in);

        clearConsole();
        System.out.println("============ Listen to Music ============\n");
        boolean check = services.showSubcribedChanels(channels);
        if (check == false)
            return;
        //todo: show file in chanel
        System.out.print("Enter your key: ");
        int key = scanner.nextInt();
        List<String> songs = services.showSongs(key);

        System.out.println("Note: Press 0 to back Home Menu");
        System.out.print("Enter your key: ");
        key = scanner.nextInt();

        if(key == 0){
            return;
        }
        String pathFile = "resources/" + songs.get(0) + "/" + songs.get(key);
        MP3Player player = new MP3Player();
        player.addToPlayList(new File(pathFile));
        String pressButton;
        clearConsole();
        System.out.println("=========================== LOVE MUSIC ==========================\n\n");
        System.out.println("\t\t\t\t" + songs.get(key).toUpperCase() + " is running.......................");
        System.out.println("Note: Press 's' to stop play music");
        player.play();

        pressButton = scanner.nextLine();
        pressButton = scanner.nextLine();
        if(pressButton.equals("s")){
            player.stop();
        }
    }

    public void yourChanel(Socket socket) throws IOException {
        Scanner input = new Scanner(System.in);
        System.out.println("===================== YOUR CHANEL ===================");
        System.out.println("1. List file in your chanel");
        System.out.println("2. Upload new file");
        System.out.print("Enter key: ");
        String key = input.nextLine();
        switch (key){
            case "1":
                clearConsole();
                services.showYourfiles();
                break;
            case "2":
                clearConsole();
                services.sendPublishMessage();
                services.uploadFile();
                break;
        }
    }

    public void unSubcribeMenu(Socket socket, List<Channel> channels) throws IOException {
        Scanner input = new Scanner(System.in);
        clearConsole();
        boolean check = services.showSubcribedChanels(channels);
        if(check == false)
            return;
        System.out.print("Enter key of chanel: ");
        int key = input.nextInt();
        services.sendUnsubcribeMessage(key);
    }

    public String checkExistChanel() throws IOException {
        File file = new File("resources/client.txt");
        BufferedReader br = new BufferedReader(new FileReader(file));

        String tmp = "";
        for(int i = 0; i < 3; i++){
            tmp = br.readLine();
        }

        if(tmp.equals("nameChanel:")){ // don't exist chanel
            return "null";
        }

        return tmp.split(":")[1]; // exist chanel and return nameChanel
    }



}

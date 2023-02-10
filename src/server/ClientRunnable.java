package server;

import model.Protocol;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClientRunnable implements Runnable {
    private final Socket monClient;
    private BufferedReader in;
    private PrintWriter out;
    private boolean isConnected = false;
    private final MurmurServer controller;
    private Protocol protocol;

    public ClientRunnable(Socket client, MurmurServer controller) {
        this.monClient = client;
        this.controller=  controller;
        protocol = new Protocol();
        try {
            in = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true);
            isConnected = true;
        } catch(IOException ex) { ex.printStackTrace(); }
    }

    public void run() {
        try {
            //Récuperer l'adresse du client et envoyer un message de bienvenue
            String clientAddress = monClient.getInetAddress().getHostAddress();
            sendMessage(protocol.build_hello_message(clientAddress));
            String ligne = in.readLine();
            System.out.printf("Ligne reçue : %s\r\n", ligne);
            while(isConnected && ligne != null) {

                //if (protocol.parse(ligne,true)==Protocol.PA)
                controller.broadcastToAllClientsExceptMe(this, ligne);
                //Vérifier si la ligne recue est la commande register grace a la classe Protocol
//                if(protocol.isRegisterCommand(ligne)) {
//                    //Si c'est le cas, on envoie un message de bienvenue
//                    sendMessage(protocol.getWelcomeMessage());
//                }
                sendMessage(protocol.build_hello_message("localhost"));
                ligne = in.readLine();
            }
        } catch(IOException ex) { ex.printStackTrace(); }
    }

    public void sendMessage(String message) {
        if(isConnected) {
            out.println(message);
            out.flush();
            // System.out.printf("Message envoyé: %s\n", message);
        }
    }
}

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

                String[] tab = protocol.parse_Register(ligne);
                //TODO : vérifier que le sel et le hash sont corrects si oui envoyer +OK sinon -ERR
                sendMessage("+OK\r\n");

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

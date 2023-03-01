package org.server;

import org.model.Protocol;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientRunnable implements Runnable {
    private final Socket monClient;
    private BufferedReader in;
    private PrintWriter out;
    private boolean isConnected = false;
    private final MurmurServer controller;
    private Protocol protocol;
    private String randomCaract;

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
            //Récuperer l'adresse du client et construis le message hello aléatoire
            String clientAddress = monClient.getInetAddress().getHostAddress();
            String helloMsg = protocol.build_hello_message(clientAddress);

            // divise le helloMsg et l'envoie, récupère les 22 caractères aléatoires et les stock dans randomCaract
            //TODO : Modifier comment je récupère le string de la regex
            String[] random = helloMsg.split(" ");
            sendMessage(helloMsg);
            this.randomCaract = random[2];


            String ligne = in.readLine();
            System.out.printf("Ligne reçue : %s\r\n", ligne);
            while(isConnected && ligne != null) {
                if (ligne.matches(protocol.getRxRegister())){
                    System.out.println("Register");
                    sendMessage("+OK\r\n");
                }
                if(ligne.matches(protocol.getRxConnect())){
                    System.out.println("Connect");
                    Pattern pattern = Pattern.compile(protocol.getRxConnect());
                    Matcher matcher = pattern.matcher(ligne);
                    String name = matcher.group(1);
                    connectUser(name);
                    sendMessage("+OK\r\n");
                }

                //TODO : CHANGER COMMENT GET HELLO CARACT STRING
                //TODO : CONNECTUSER


                //TODO : vérifier que le sel et le hash sont corrects si oui envoyer +OK sinon -ERR



                ligne = in.readLine();
            }
        } catch(IOException ex) { ex.printStackTrace(); }
    }

    public void connectUser(String name){
        String sha3hash;

        //TODO : getUserByName

        //Envoie un message connect avec le round et le sel
        sendMessage(protocol.build_connect_message(user.getRound,user.getSel));

        //Calcule le sha3 sur base des 22 caractères du message HELLO, et de la chaineHashBcrypt
        try{
            MessageDigest messDigest = MessageDigest.getInstance("SHA3-256");
            byte[] digest = messDigest.digest((this.randomCaract+user.getChainHashBcrypt).getBytes());
            sha3hash = new String(digest);
        }catch (Exception e){
            System.out.println("Erreur dans le sha3-256");
        }



        //TODO : calculer le sha3 de ce coté
        //TODO : Vérifier l'égalisation de confirm et du sha

    }

    public void sendMessage(String message) {
        if(isConnected) {
            out.println(message);
            out.flush();
            // System.out.printf("Message envoyé: %s\n", message);
        }
    }
}

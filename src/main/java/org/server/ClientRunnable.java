package org.server;

import org.model.*;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;
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
    private final ApplicationData appData;
    private String login;


    public ClientRunnable(Socket client, MurmurServer controller) {
        this.monClient = client;
        this.controller=  controller;
        appData = Json.getAppData();
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
                    //TODO : sauv user
                    Pattern pattern = Pattern.compile(protocol.getRxRegister());
                    Matcher matcher = pattern.matcher(ligne);
                    if(matcher.find()){
                        login = matcher.group(1);
                    }
                    sendMessage("+OK\r\n");
                }
                if(ligne.matches(protocol.getRxConnect())){
                    System.out.println("Connect");

                    //TODO : ça marche vraiment ?
                    Pattern pattern = Pattern.compile(protocol.getRxConnect());
                    Matcher matcher = pattern.matcher(ligne);
                    login = matcher.group(1);
                    connectUser(login);
                    sendMessage("+OK\r\n");
                }

                //TODO : RÉCUPERER LE MESSAGE ET L'AJOUTTER AU FOLLOW


                //TODO : CHANGER COMMENT GET HELLO CARACT STRING
                //TODO : CONNECTUSER


                //TODO : vérifier que le sel et le hash sont corrects si oui envoyer +OK sinon -ERR



                ligne = in.readLine();
            }
        } catch(IOException ex) { ex.printStackTrace(); }
    }

    public static void main(String[] args) {
        String ligne = "FOLLOW jeans@serv1.godwsilla.guru";
        Protocol protocol = new Protocol();
        ApplicationData appData = Json.getAppData();
        String login = "romain";

        if (ligne.matches(protocol.getRxFollow())){
            Pattern pattern = Pattern.compile(protocol.getRxFollow());
            Matcher matcher = pattern.matcher(ligne);
            if (matcher.find()){
                String group = matcher.group(1);
                System.out.println(group);
                if (group.matches(protocol.getRxUserDomain())){
                    List<String> followers = appData.getUser(login).getFollowers();
                    //vérifier si la liste contient déjà le follow
                    if (followers.contains(group)){
                        System.out.println("Vous suivez déjà cet utilisateur");
                    }
                    else {
                        followers.add(group);
                        appData.getUser(login).setFollowers(followers);
                        //affiche les variables de appData
                        System.out.println(appData.getUser(login).getFollowers());
                    }


                }
                if (group.equals(protocol.getRxTagDomain())){
                    List<String> tags = appData.getUser(login).getUserTags();
                    tags.add(group);
                    appData.getUser(login).setUserTags(tags);
                    List<Tag> tagList = appData.getTags();
                    int i = 0;
                    for (Tag tag : tagList){
                        if (tag.getTag().equals(group)){
                            List<String> users = tag.getFollowers();
                            users.add(login);
                            tag.setFollowers(users);
                            i++;
                        }
                    }
                    if (i == 0){
                        Tag newTag = new Tag(group, List.of(login));
                        tagList.add(newTag);
                    }
                }
                Json.sauvegarder(appData);
            }
        }
    }

    public void connectUser(String name){
        String sha3hash;

        Utilisateur user = Json.getUser(name);

        if(user == null){
            sendMessage("-ERR\r\n");
        }else{
            //Envoie un message connect avec le round et le sel
            sendMessage(protocol.build_connect_message(user.getBcryptRound(),user.getBcryptSalt()));

            //Calcule le sha3 sur base des 22 caractères du message HELLO, et de la chaineHashBcrypt
            try{
                MessageDigest messDigest = MessageDigest.getInstance("SHA3-256");
                byte[] digest = messDigest.digest((this.randomCaract+user.getBcryptHash()).getBytes());
                sha3hash = new String(digest);
            }catch (Exception e){
                System.out.println("Erreur dans le sha3-256");
            }

            //TODO : Vérifier l'égalisation de confirm et du sha
        }

    }

    public void sendMessage(String message) {
        if(isConnected) {
            out.println(message);
            out.flush();
            // System.out.printf("Message envoyé: %s\n", message);
        }
    }
}

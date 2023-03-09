package org.MurmurServer.server;

import org.MurmurServer.model.ApplicationData;
import org.MurmurServer.model.*;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ClientRunnable implements Runnable {
    private final Socket monClient;
    private BufferedReader in;
    private PrintWriter out;
    private boolean isConnected = false;
    private final MurmurServer controller;
    private final Protocol protocol;
    private String randomCaract;
    private ApplicationData applicationData;
    private String shaCalculated;
    private Utilisateur user;


    public ClientRunnable(Socket client, MurmurServer controller) {
        this.applicationData = Json.getApplicationData();
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
            String clientAddress = applicationData.getCurrentDomain();
            String helloMsg = protocol.build_hello_message(clientAddress);

            //Envoie le message de bienvenue (connexion entre app)
            System.out.println(helloMsg);
            sendMessage(helloMsg);

            //Récupère les caractères aléatoire du message HELLO et les stocke dans randomCaract
            Pattern patternH = Pattern.compile(protocol.getRxHello());
            Matcher matcherH = patternH.matcher(helloMsg);
            if (matcherH.find()) {
                this.randomCaract = matcherH.group(3);
            }

            //Récupère l'action de base du client
            String ligne = in.readLine();
            //TODO : DECRYPTER LA LIGNE
            System.out.printf("Ligne reçue : %s\r\n", ligne);

            //Tant que le client est connecté et qu'il envoie des informations :
            while(isConnected && ligne != null) {
                //Enregistrement d'un utilisateur
                if (ligne.matches(protocol.getRxRegister())){
                    System.out.println("Register");

                    // Créer un objet Pattern et Matcher pour la regex protocol.getRxRegister()
                    Pattern pattern = Pattern.compile(protocol.getRxRegister());
                    Matcher matcher = pattern.matcher(ligne);

                    // Récupérer la valeur de RX_ESP à partir de la chaîne ligne
                    if (matcher.find()) {
                        String rx_username = matcher.group(1);
                        String rx_hash = matcher.group(9);
                        int rx_round = Integer.parseInt(matcher.group(6));
                        String salt = matcher.group(7);
                        user = new Utilisateur(rx_username, rx_hash, rx_round, salt, new ArrayList<String>(), new ArrayList<String>(), 0);
                        applicationData.addUser(user);
                        Json.sauvegarder(applicationData);
                    }

                    sendMessage("+OK\r\n");
                }
                //Connexion d'un utilisateur
                if(ligne.matches(protocol.getRxConnect())){
                    String name=null;
                    System.out.println("Connect");

                    //Récupère le nom d'utilisateur
                    Pattern pattern = Pattern.compile(protocol.getRxConnect());
                    Matcher matcher = pattern.matcher(ligne);
                    if (matcher.find()) {
                        name = matcher.group(1);
                    }
                    connectUser(name);

                }
                //Confirmation de connexion
                if(ligne.matches(protocol.getRxConfirm())){
                    String shaRecieved=null;
                    Pattern pattern = Pattern.compile(protocol.getRxConfirm());
                    Matcher matcher = pattern.matcher(ligne);
                    if (matcher.find()) {
                        shaRecieved = matcher.group(1);
                    }
                    assert shaRecieved != null;
                    if(shaRecieved.equals(shaCalculated)){
                        sendMessage("+OK\r\n");
                    }else{
                        sendMessage("-ERR\r\n");
                    }
                }if (ligne.matches(protocol.getRxDisconnect())) {
                    sendMessage("+OK\r\n");
                }
                //Gestion des follows
                if (ligne.matches(protocol.getRxFollow())){
                    Pattern pattern = Pattern.compile(protocol.getRxFollow());
                    Matcher matcher = pattern.matcher(ligne);
                    if (matcher.find()){
                        String group = matcher.group(1);
                        System.out.println(group);
                        if (group.matches(protocol.getRxUserDomain())){
                            List<String> followers = applicationData.getUser(user.getLogin()).getFollowers();
                            //vérifier si la liste contient déjà le follow
                            if (followers.contains(group)){
                                System.out.println("Vous suivez déjà cet utilisateur");
                            }
                            else {
                                followers.add(group);
                                applicationData.getUser(user.getLogin()).setFollowers(followers);
                                //affiche les variables de appData
                                System.out.println(applicationData.getUser(user.getLogin()).getFollowers());
                            }


                        }
                        if (group.matches(protocol.getRxTagDomain())){
                            System.out.println("test");
                            List<String> tags = applicationData.getUser(user.getLogin()).getUserTags();
                            tags.add(group);
                            applicationData.getUser(user.getLogin()).setUserTags(tags);
                            List<Tag> tagList = applicationData.getTags();
                            int i = 0;
                            for (Tag tag : tagList){
                                if (tag.getTag().equals(group)){
                                    List<String> users = tag.getFollowers();
                                    users.add(user.getLogin()+"@"+applicationData.getCurrentDomain());
                                    tag.setFollowers(users);
                                    i++;
                                }
                            }
                            if (i == 0 && protocol.matchesWithServDomain(group, applicationData.getCurrentDomain())){
                                Tag newTag = new Tag(group, List.of(user.getLogin()+"@"+applicationData.getCurrentDomain()));
                                tagList.add(newTag);
                            }
                            else {
                                //TODO : envoyer au relais
                            }
                        }
                        Json.sauvegarder(applicationData);
                    }
                }
                //Gestion des messages
                if (ligne.matches(protocol.getRxMessage())){
                    applicationData = Json.getApplicationData();
                    //separer le message en 3 groupes
                    Pattern pattern = Pattern.compile(protocol.getRxMessage());
                    Matcher matcher = pattern.matcher(ligne);
                    if(matcher.find()){
                        String group1 = matcher.group(1);
                        //Chercher dans les users si il y en a un qui a l'utilisateur comme follower
                        List<Utilisateur> users = applicationData.getUsers();
                        List<Utilisateur> usersToBroadcast = new ArrayList<>();
                        for (Utilisateur user : users){
                            if (user.getFollowers().contains(this.user.getLogin()+"@"+applicationData.getCurrentDomain())){
                                usersToBroadcast.add(user);
                                System.out.println("Message envoyé à "+user.getLogin());
                            }
                        }
                        //Récuperer les tags de l'user et verifier dans les tags si il y en a un qui a le tag comme follower
                        List<String> tags = applicationData.getUser(this.user.getLogin()).getUserTags();//TODO : vérifier si ça marche
                        for (String tag : tags){
                            for (Tag tag1 : applicationData.getTags()){
                                if (tag1.getTag().equals(tag)){
                                    for (String user : tag1.getFollowers()){
                                        System.out.println("Message envoyé à "+user);
                                        usersToBroadcast.add(Json.getUser(user.split("@")[0]));}
                                }
                            }
                        }

                        //Vérifier qu'un utilisateur n'est pas dans la liste 2 fois
                        usersToBroadcast = usersToBroadcast.stream().distinct().collect(Collectors.toList());
                        //afficher les users qui vont recevoir le message
                        for (Utilisateur user : usersToBroadcast){
                            System.out.println(user.getLogin());
                        }
                        controller.broadcastToAllClientsExceptMe(usersToBroadcast, Protocol.createMessage(this.user.getLogin()+"@"+applicationData.getCurrentDomain(), group1), this);
                    }
                }



                ligne = in.readLine();
                System.out.printf("Ligne reçue : %s\r\n", ligne);
            }
        } catch(IOException ex) {
            System.out.println("Connexion perdue");
        }
    }


    /**
     * Récupère et stocke l'utilisateur qui tente de se connecter, lui envoie les informations pour calculer le hash
     * Calcule le hash et le stocke
     * @param name nom d'utilisateur
     */
    public void connectUser(String name){
        String sha3hash;

        user = Json.getUser(name);

        //Si utilisateur pas trouvé dans le fichier, -ERR
        if(user == null){
            System.out.printf("Erreur");
            sendMessage("-ERR\r\n");
        }else{
            //Envoie un message param avec le round et le sel
            sendMessage(protocol.build_param_message(user.getBcryptRound(),user.getBcryptSalt()));

            //Calcule le sha3 sur base des 22 caractères du message HELLO, et de la chaineHashBcrypt
            try{
                MessageDigest messDigest = MessageDigest.getInstance("SHA3-256");
                byte[] digest = messDigest.digest((this.randomCaract+"$2b$"+user.getBcryptRound()+"$"+user.getBcryptSalt()+user.getBcryptHash()).getBytes());
                BigInteger bigInt = new BigInteger(1, digest);
                shaCalculated = bigInt.toString(16);
                // pad with leading zeros for a length of 64
                while (shaCalculated.length() < 64) {
                    shaCalculated = "0" + shaCalculated;
                }
                //Calcule le SHA256 mais ne le sort pas en hexadigit
                //shaCalculated = new String(digest);
            }catch (Exception e){
                System.out.println("Erreur dans le sha3-256");
            }
        }

    }

    public void sendMessage(String message) {
        if(isConnected) {
            out.println(message);
            out.flush();
            // System.out.printf("Message envoyé: %s\n", message);
        }
    }


    public Utilisateur getUser() {
        return user;
    }
}

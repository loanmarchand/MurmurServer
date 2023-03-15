package org.MurmurServer.model;

import org.MurmurRelay.utils.AesUtils;
import org.MurmurServer.server.ClientRunnable;
import org.MurmurServer.server.MurmurServer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommandServer {

    private Protocol protocol;
    private ApplicationData applicationData;
    private Utilisateur user;
    private Json json;
    private MurmurServer controller;
    private ClientRunnable clientRunnable;
    private AesUtils aesUtils;

    public CommandServer(Protocol protocol, ApplicationData applicationData, Utilisateur user, Json json, MurmurServer controller,ClientRunnable clientRunnable) {
        this.protocol = protocol;
        this.applicationData = applicationData;
        this.user = user;
        this.json = json;
        this.controller = controller;
        this.clientRunnable = clientRunnable;
        aesUtils = new AesUtils();
    }

    public void sendFollow(String ligne) throws Exception {
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
                    // TODO : transformer ligne pour l'inclure dans SEND
                    // Doit ce transformer en sa :
                    // SEND 1234 server1.godswila.guru server2.godswila.guru FOLLOW michel@server2.godswila.guru
                    Pattern pattern1 = Pattern.compile(protocol.getRxFollow());
                    Matcher matcher1 = pattern1.matcher(ligne);
                    if (matcher1.find()){
                        String groupe = matcher1.group(1);
                        String message = "SEND 1234 " + applicationData.getCurrentDomain() + " " + groupe + " " + ligne;
                        String cryptedMessage = aesUtils.encrypt(message, controller.getSecretKey());
                        controller.sendToRelay(cryptedMessage);
                    }
                }
            }
            json.sauvegarder(applicationData);
        }
    }

    public void sendMsg(String ligne) {
        applicationData = json.getApplicationData();
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
            List<String> tags = applicationData.getUser(this.user.getLogin()).getUserTags();
            for (String tag : tags){
                for (Tag tag1 : applicationData.getTags()){
                    if (tag1.getTag().equals(tag)){
                        for (String user : tag1.getFollowers()){
                            System.out.println("Message envoyé à "+user);
                            usersToBroadcast.add(json.getUser(user.split("@")[0]));}
                    }
                }
            }

            //Vérifier qu'un utilisateur n'est pas dans la liste 2 fois
            usersToBroadcast = usersToBroadcast.stream().distinct().collect(Collectors.toList());
            //afficher les users qui vont recevoir le message
            for (Utilisateur user : usersToBroadcast){
                System.out.println(user.getLogin());
            }
            controller.broadcastToAllClientsExceptMe(usersToBroadcast, protocol.createMessage(this.user.getLogin()+"@"+applicationData.getCurrentDomain(), group1), clientRunnable);
        }
    }
}

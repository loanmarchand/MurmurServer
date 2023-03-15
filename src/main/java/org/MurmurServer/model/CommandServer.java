package org.MurmurServer.model;

import org.MurmurRelay.utils.AesUtils;
import org.MurmurServer.server.ClientRunnable;
import org.MurmurServer.server.MurmurServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommandServer {
    private Protocol protocol;
    private Json json;
    private AesUtils aesUtils;

    public CommandServer() {
        protocol = new Protocol();
        json = new Json();
        aesUtils = new AesUtils();
    }

    public void sendFollow(String ligne,ApplicationData applicationData, Utilisateur user,MurmurServer controller) throws Exception {
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
                    // Doit ce transformer en ça :
                    // SEND 1234 server1.godswila.guru server2.godswila.guru FOLLOW loans michel@server2.godswila.guru
                    //FOLLOW user.getName() #test@server2.godswila.guru
                    Pattern pattern1 = Pattern.compile(protocol.getRxFollow());
                    Matcher matcher1 = pattern1.matcher(ligne);
                    if (matcher1.find()){
                        String groupe1 = matcher1.group(1);
                        String groupe2 = matcher1.group(2);
                        String message = "SEND 1234 " + applicationData.getCurrentDomain() + " " + groupe2 + " Follow " + user.getLogin() + groupe1;
                        String cryptedMessage = aesUtils.encrypt(message, controller.getSecretKey());
                        controller.sendToRelay(cryptedMessage);
                    }
                }
            }
            json.sauvegarder(applicationData);
        }
    }

    public void sendMsg(String ligne, Utilisateur usera,MurmurServer controller,ClientRunnable clientRunnable) throws Exception {
        ApplicationData applicationData = json.getApplicationData();

        Pattern pattern = Pattern.compile(protocol.getRxMessage());
        Matcher matcher = pattern.matcher(ligne);
        if(matcher.find()){
            String group1 = matcher.group(1);
            //Chercher dans les users si il y en a un qui a l'utilisateur comme follower
            List<Utilisateur> users = applicationData.getUsers();
            List<Utilisateur> usersToBroadcast = new ArrayList<>();
            Map<Utilisateur,String> usersToSendRelay = new HashMap<>();
            for (String follower:usera.getFollowers()){
                Pattern pattern1 = Pattern.compile(protocol.getRxUserDomain());
                Matcher matcher1 = pattern1.matcher(follower);
                if (matcher1.find()){
                    String group = matcher1.group(1);
                    String domain = matcher1.group(2);
                    if (protocol.matchesWithServDomain(domain, applicationData.getCurrentDomain())){
                        usersToBroadcast.add(json.getUser(group));
                    }
                    else {
                        usersToSendRelay.put(json.getUser(group),domain);
                    }
                }

            }
            //Récuperer les tags de l'user et verifier dans les tags si il y en a un qui a le tag comme follower
            List<String> tags = usera.getUserTags();
            for (String tag : tags){
                Pattern pattern1 = Pattern.compile(protocol.getRxTagDomain());
                Matcher matcher1 = pattern1.matcher(tag);
                if (matcher1.find()){
                    String domain = matcher1.group(2);
                    if(protocol.matchesWithServDomain(domain, applicationData.getCurrentDomain())){
                        for (Tag tag1 : applicationData.getTags()){
                            if (tag.equals(tag1.getTag())){
                                for (String follower : tag1.getFollowers()){
                                    Pattern pattern2 = Pattern.compile(protocol.getRxUserDomain());
                                    Matcher matcher3 = pattern1.matcher(follower);
                                    if (matcher1.find()){
                                        String group = matcher1.group(1);
                                        String domains = matcher1.group(2);
                                        if (protocol.matchesWithServDomain(domains, applicationData.getCurrentDomain())){
                                            usersToBroadcast.add(json.getUser(group));
                                        }
                                        else {
                                            usersToSendRelay.put(json.getUser(group),domains);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else {
                        String message = "SEND 1234 " + applicationData.getCurrentDomain() + " " + tag + " MSGS " + usera.getLogin() + group1;
                        String cryptedMessage = aesUtils.encrypt(message, controller.getSecretKey());
                        controller.sendToRelay(cryptedMessage);
                    }
                }

            }

            //Vérifier qu'un utilisateur n'est pas dans la liste 2 fois
            usersToBroadcast = usersToBroadcast.stream().distinct().collect(Collectors.toList());
            usersToSendRelay = usersToSendRelay.entrySet().stream().distinct().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            //afficher les users qui vont recevoir le message
            for (Utilisateur user : usersToBroadcast){
                System.out.println(user.getLogin());
            }
            controller.broadcastToAllClientsExceptMe(usersToBroadcast, protocol.createMessage(usera.getLogin()+"@"+applicationData.getCurrentDomain(), group1), clientRunnable);
            for (Map.Entry<Utilisateur,String> entry : usersToSendRelay.entrySet()){
                String message = "SEND 1234 " + applicationData.getCurrentDomain() + " " + entry.getValue() + " MSGS " + usera.getLogin() + group1;
                String cryptedMessage = aesUtils.encrypt(message, controller.getSecretKey());
                controller.sendToRelay(cryptedMessage);
            }
        }
    }

    public void followTagRelay(String ligne, String user, String domain) {
        ApplicationData applicationData = json.getApplicationData();
        List<Tag> tagList = applicationData.getTags();
        int i = 0;
        for (Tag tag : tagList){
            if (tag.getTag().equals(ligne)){
                List<String> users = tag.getFollowers();
                users.add(user+"@"+domain);
                tag.setFollowers(users);
                i++;
            }
        }
        if (i == 0 && protocol.matchesWithServDomain(ligne, applicationData.getCurrentDomain())){
            Tag newTag = new Tag(ligne, List.of(user+"@"+domain));
            tagList.add(newTag);
        }
        json.sauvegarder(applicationData);
    }
}

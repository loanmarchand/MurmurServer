package org.MurmurServer.model;

import org.MurmurRelay.utils.AesUtils;
import org.MurmurServer.server.ClientRunnable;
import org.MurmurServer.server.MurmurServer;

import java.util.*;
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

    public void sendFollow(String ligne, String user, MurmurServer controller) throws Exception {
        Pattern pattern = Pattern.compile(protocol.getRxFollow());
        Matcher matcher = pattern.matcher(ligne);
        ApplicationData applicationData = json.getApplicationData();
        if (matcher.find()) {
            String group = matcher.group(1);

            System.out.println(group);
            if (group.matches(protocol.getRxUserDomain())) {
                Pattern pattern1 = Pattern.compile(protocol.getRxUserDomain());
                Matcher matcher1 = pattern1.matcher(group);
                if (matcher1.find()) {
                    String domain = matcher1.group(4);
                    String login = matcher1.group(2);
                    if (applicationData.getCurrentDomain().equals(domain)) {
                        Utilisateur user1 = applicationData.getUser(login);
                        if (user1 != null) {
                            List<String> followers = applicationData.getUser(user1.getLogin()).getFollowers();
                            if (followers.contains(group)) {
                                System.out.println("Vous suivez déjà cet utilisateur");
                            } else {
                                followers.add(user + "@" + domain);
                                applicationData.getUser(user1.getLogin()).setFollowers(followers);
                                json.sauvegarder(applicationData);
                                //affiche les variables de appData
                                System.out.println(applicationData.getUser(user1.getLogin()).getFollowers());
                            }
                        } else {
                            System.out.println("Cet utilisateur n'existe pas");
                        }
                    } else {
                        String message = "SEND 1234 " + applicationData.getCurrentDomain() + " " + domain + " FOLLOW " + user + " " + matcher1.group(1);
                        String cryptedMessage = aesUtils.encrypt(message, controller.getSecretKey());
                        controller.sendToRelay(cryptedMessage);
                        System.out.println("Message envoyé au relay ");
                    }
                }


            }
            //TODO : A CHANGER NE MARCHE PAS
            if (group.matches(protocol.getRxTagDomain())) {
                System.out.println("test");
                Pattern pattern2 = Pattern.compile(protocol.getRxTagDomain());
                Matcher matcher2 = pattern2.matcher(group);
                String domain = "";
                if (matcher2.find()){
                    domain = matcher2.group(2);
                }
                List<String> tags = applicationData.getUser(user).getUserTags();
                List<String> tagsToAdd = new ArrayList<>();
                if (tags.isEmpty()){
                    tags.add(group);
                    applicationData.getUser(user).setUserTags(tags);
                }
                else {
                    System.out.println(applicationData.getUser(user).getUserTags());
                    for (String tag : tags){
                        if (tag.equals(group)){
                            System.out.println("Vous suivez déjà ce groupe");
                        }
                        else {
                            tagsToAdd.add(group);
                        }
                    }
                    tags.addAll(tagsToAdd);
                    applicationData.getUser(user).setUserTags(tags);
                }
                List<Tag> tagList = applicationData.getTags();
                int i = 0;
                for (Tag tag : tagList) {
                    if (tag.getTag().equals(group)) {
                        List<String> users = tag.getFollowers();
                        if (users.contains(user + "@" + applicationData.getCurrentDomain())) {
                            System.out.println("Vous suivez déjà ce groupe");
                        } else {
                            users.add(user + "@" + applicationData.getCurrentDomain());
                            tag.setFollowers(users);
                            System.out.println(tag.getFollowers());
                        }
                        i++;
                    }
                }
                applicationData.setTags(tagList);
                System.out.println(i);
                System.out.println(domain);
                if (i == 0 && domain.equals(applicationData.getCurrentDomain())) {//JE PENSE ICI QUE C'EST LA QUE CA BUG
                    Tag newTag = new Tag(group, List.of(user + "@" + applicationData.getCurrentDomain()));
                    tagList.add(newTag);
                    applicationData.setTags(tagList);
                }
                else if (!domain.equals(applicationData.getCurrentDomain())){
                    // TODO : transformer ligne pour l'inclure dans SEND
                        String message = "SEND 1234 " + applicationData.getCurrentDomain() + " " + domain + " FOLLOW " + user + " " + group;
                        String cryptedMessage = aesUtils.encrypt(message, controller.getSecretKey());
                        controller.sendToRelay(cryptedMessage);

                }
                json.sauvegarder(applicationData);
            }

        }
    }

    public void sendMsg(String ligne, String usera, MurmurServer controller) throws Exception {
        ApplicationData applicationData = json.getApplicationData();

        Pattern pattern = Pattern.compile(protocol.getRxMessage());
        Matcher matcher = pattern.matcher(ligne);
        List<String> usersToSend = new ArrayList<>();
        List<String> usersToSendRelay = new ArrayList<>();
        List<String> tagsToSend = new ArrayList<>();
        System.out.println(ligne);
        if (matcher.find()) {
            System.out.println("test");
            List<String> users = applicationData.getUser(usera).getFollowers();
            for (String user : users) {
                System.out.println(user);
                Pattern pattern1 = Pattern.compile(protocol.getRxUserDomain());
                Matcher matcher1 = pattern1.matcher(user);
                if (matcher1.find()) {
                    System.out.println("testa");
                    String domain = matcher1.group(4);
                    System.out.println(domain);
                    if (domain.equals(applicationData.getCurrentDomain())) {
                        System.out.println("testb");
                        usersToSend.add(user);
                    } else {
                        usersToSendRelay.add(user);

                    }

                }
            }
            List<String> tags = applicationData.getUser(usera).getUserTags();
            for (String tag : tags) {
                Pattern pattern1 = Pattern.compile(protocol.getRxTagDomain());
                Matcher matcher1 = pattern1.matcher(tag);
                if (matcher1.find()) {
                    String domain = matcher1.group(2);
                    if (domain.equals(applicationData.getCurrentDomain())) {
                        List<Tag> tagList = applicationData.getTags();
                        for (Tag tag1 : tagList) {
                            if (tag1.getTag().equals(tag)) {
                                List<String> users1 = tag1.getFollowers();
                                usersToSend.addAll(users1);
                            }
                        }

                    } else {
                        tagsToSend.add(tag);
                    }
                }
            }

            controller.broadcastToAllClients(usersToSend, protocol.createMessage(usera+"@"+applicationData.getCurrentDomain(), matcher.group(1)));

            List<String> cryptedMessages = new ArrayList<>();
            //Construction message send followers
            List<String> domains = new ArrayList<>();
            for (String user : usersToSendRelay) {
                Pattern pattern1 = Pattern.compile(protocol.getRxUserDomain());
                Matcher matcher1 = pattern1.matcher(user);
                if (matcher1.find()) {
                    String domain = matcher1.group(4);
                    String message = "SEND 1234 " + applicationData.getCurrentDomain() + " " + domain + " MSGS " + usera + "@" + applicationData.getCurrentDomain() + " " + user + " " + matcher.group(1);
                    String cryptedMessage = aesUtils.encrypt(message, controller.getSecretKey());
                    cryptedMessages.add(cryptedMessage);
                }
            }
            //Nettoyage des doublons de cryptedMessages
            Set<String> hs = new HashSet<>(cryptedMessages);
            cryptedMessages.clear();
            cryptedMessages.addAll(hs);
            for (String cryptedMessage : cryptedMessages) {
                controller.sendToRelay(cryptedMessage);
            }

            //Construction message send tags
            List<String> domains1 = new ArrayList<>();
            for (String tag : tagsToSend) {
                Pattern pattern1 = Pattern.compile(protocol.getRxTagDomain());
                Matcher matcher1 = pattern1.matcher(tag);
                if (matcher1.find()) {
                    String domain = matcher1.group(2);
                    domains1.add(domain);
                }
            }

        }


    }

    public void sendMsgTest(String ligne, String usera, MurmurServer murmurServer){
        Pattern pattern = Pattern.compile(protocol.getRxMessage());
        Matcher matcher = pattern.matcher(ligne);
        String message = "";
        if (matcher.find()) {
            message = matcher.group(1);
        }
        List<String> userToSendSameServer = new ArrayList<>();
        List<String> userToSendOtherServer = new ArrayList<>();

        ApplicationData applicationData = json.getApplicationData();

        List<String> followers = applicationData.getUser(usera).getFollowers();

        for (String follower : followers){
            Pattern pattern1 = Pattern.compile(protocol.getRxUserDomain());
            Matcher matcher1 = pattern1.matcher(follower);
            if (matcher1.find()){
                String domain = matcher1.group(4);
                if (domain.equals(applicationData.getCurrentDomain())){
                    userToSendSameServer.add(protocol.getUSernameFromUserDomain(follower));
                }
                else {
                    userToSendOtherServer.add(follower);
                }
            }
        }
        System.out.println(userToSendSameServer);
        murmurServer.broadcastToAllClients(userToSendSameServer, protocol.createMessage(usera+"@"+applicationData.getCurrentDomain(), message));
    }


    public void followTagRelay(String ligne, String user) {
        System.out.println(ligne);
        System.out.println(user);
        ApplicationData applicationData = json.getApplicationData();
        List<Tag> tagList = applicationData.getTags();
        int i = 0;
        for (Tag tag : tagList) {
            if (tag.getTag().equals(ligne)) {
                List<String> users = tag.getFollowers();
                users.add(user);
                tag.setFollowers(users);
                i++;
            }
        }
        if (i == 0 && protocol.matchesWithServDomain(ligne, applicationData.getCurrentDomain())) {
            Tag newTag = new Tag(ligne, List.of(user));
            tagList.add(newTag);
        }
        json.sauvegarder(applicationData);
    }

    public void sendFollowUser(String ligne, String user) {
        Pattern pattern = Pattern.compile(protocol.getRxFollow());
        Matcher matcher = pattern.matcher(ligne);
        ApplicationData applicationData = json.getApplicationData();
        if (matcher.find()) {
            String group = matcher.group(1);
            System.out.println(group);
            Utilisateur user1 = applicationData.getUser(user);
            if (user1 != null) {
                List<String> followers = applicationData.getUser(user1.getLogin()).getFollowers();
                if (followers.contains(group)) {
                    System.out.println("Vous suivez déjà cet utilisateur");
                } else {
                    followers.add(group);
                    applicationData.getUser(user).setFollowers(followers);
                    //affiche les variables de appData
                    System.out.println(applicationData.getUser(user1.getLogin()).getFollowers());
                }
            } else {
                System.out.println("Cet utilisateur n'existe pas");
            }

        }


        json.sauvegarder(applicationData);
    }

    public void sendMsgRelay(String userWhoSend, String userWhoReceived,String message, MurmurServer murmurServer) {
        ApplicationData applicationData = json.getApplicationData();
        Pattern pattern = Pattern.compile(protocol.getRxUserDomain());
        Matcher matcher = pattern.matcher(userWhoReceived);
        if (matcher.find()) {
            String domain = matcher.group(4);
            if (domain.equals(applicationData.getCurrentDomain())) {
                murmurServer.broadcastToAllClients(List.of(userWhoReceived), protocol.createMessage(userWhoSend, message));
            }
        }

    }
}

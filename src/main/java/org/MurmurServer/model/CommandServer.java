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
                    }
                }


            }
            if (group.matches(protocol.getRxTagDomain())) {
                System.out.println("test");
                String domain = matcher.group(2);
                List<String> tags = applicationData.getUser(user).getUserTags();
                tags.add(group);
                applicationData.getUser(user).setUserTags(tags);
                List<Tag> tagList = applicationData.getTags();
                int i = 0;
                for (Tag tag : tagList) {
                    if (tag.getTag().equals(group)) {
                        List<String> users = tag.getFollowers();
                        users.add(user + "@" + applicationData.getCurrentDomain());
                        tag.setFollowers(users);
                        i++;
                    }
                }
                if (i == 0 && domain.equals(applicationData.getCurrentDomain())) {
                    Tag newTag = new Tag(group, List.of(user + "@" + applicationData.getCurrentDomain()));
                    tagList.add(newTag);
                } else {
                    // TODO : transformer ligne pour l'inclure dans SEND
                    // Doit ce transformer en ça :
                    // SEND 1234 server1.godswila.guru server2.godswila.guru FOLLOW loans michel@server2.godswila.guru
                    //FOLLOW user.getName() #test@server2.godswila.guru
                    Pattern pattern1 = Pattern.compile(protocol.getRxFollow());
                    Matcher matcher1 = pattern1.matcher(ligne);
                    if (matcher1.find()) {
                        String groupe1 = matcher1.group(1);
                        String groupe2 = matcher1.group(3);
                        String message = "SEND 1234 " + applicationData.getCurrentDomain() + " " + groupe2 + " FOLLOW " + user + " " + groupe1;
                        String cryptedMessage = aesUtils.encrypt(message, controller.getSecretKey());
                        controller.sendToRelay(cryptedMessage);
                    }
                }
            }
            json.sauvegarder(applicationData);
        }
    }

    public void sendMsg(String ligne, String usera, MurmurServer controller) throws Exception {
        ApplicationData applicationData = json.getApplicationData();

        Pattern pattern = Pattern.compile(protocol.getRxMessage());
        Matcher matcher = pattern.matcher(ligne);
        List<String> usersToSend = new ArrayList<>();
        List<String> usersToSendRelay = new ArrayList<>();
        List<String> tagsToSend = new ArrayList<>();
        if (matcher.find()) {
            List<String> users = applicationData.getUser(usera).getFollowers();
            for (String user : users) {
                Pattern pattern1 = Pattern.compile(protocol.getRxUserDomain());
                Matcher matcher1 = pattern1.matcher(user);
                if (matcher.find()) {
                    String domain = matcher1.group(4);
                    if (domain.equals(applicationData.getCurrentDomain())) {
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

            controller.broadcastToAllClients(usersToSend, protocol.createMessage(usera, matcher.group(1)));

            List<String> cryptedMessages = new ArrayList<>();
            //Construction message send followers
            List<String> domains = new ArrayList<>();
            for (String user : usersToSendRelay) {
                Pattern pattern1 = Pattern.compile(protocol.getRxUserDomain());
                Matcher matcher1 = pattern1.matcher(user);
                if (matcher1.find()) {
                    String domain = matcher1.group(4);
                    domains.add(domain);
                }
            }
            //Nettoyage des doublons de domaines
            Set<String> set = new HashSet<>(domains);
            domains.clear();
            domains.addAll(set);
            //Envoi des messages
            for (String domain : domains) {
                String message = "SEND 1234 " + applicationData.getCurrentDomain() + " " + domain + " MSGS " + usera + " " + matcher.group(1);
                String cryptedMessage = aesUtils.encrypt(message, controller.getSecretKey());
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


    public void followTagRelay(String ligne, String user, String domain) {
        ApplicationData applicationData = json.getApplicationData();
        List<Tag> tagList = applicationData.getTags();
        int i = 0;
        for (Tag tag : tagList) {
            if (tag.getTag().equals(ligne)) {
                List<String> users = tag.getFollowers();
                users.add(user + "@" + domain);
                tag.setFollowers(users);
                i++;
            }
        }
        if (i == 0 && protocol.matchesWithServDomain(ligne, applicationData.getCurrentDomain())) {
            Tag newTag = new Tag(ligne, List.of(user + "@" + domain));
            tagList.add(newTag);
        }
        json.sauvegarder(applicationData);
    }

    public void sendFollowUser(String ligne,String s, String user, MurmurServer murmurServer) {
        Pattern pattern = Pattern.compile(protocol.getRxFollow());
        Matcher matcher = pattern.matcher(ligne);
        Matcher matcher2 = pattern.matcher(s);
        ApplicationData applicationData = json.getApplicationData();
        if (matcher.find() && matcher2.find()) {
            String group = matcher2.group(1);

            System.out.println(group);
            if (group.matches(protocol.getRxUserDomain())) {
                Pattern pattern1 = Pattern.compile(protocol.getRxUserDomain());
                Matcher matcher1 = pattern1.matcher(group);
                if (matcher1.find()) {
                    String domain = matcher1.group(4);
                    String login = matcher1.group(2);
                    Utilisateur user1 = applicationData.getUser(login);
                    if (user1 != null) {
                        List<String> followers = applicationData.getUser(user1.getLogin()).getFollowers();
                        if (followers.contains(group)) {
                            System.out.println("Vous suivez déjà cet utilisateur");
                        } else {
                            followers.add(user + "@" + domain);
                            applicationData.getUser(user1.getLogin()).setFollowers(followers);
                            //affiche les variables de appData
                            System.out.println(applicationData.getUser(user1.getLogin()).getFollowers());
                        }
                    } else {
                        System.out.println("Cet utilisateur n'existe pas");
                    }

                }
            }
        }
    }
}

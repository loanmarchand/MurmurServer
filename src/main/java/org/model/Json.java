package org.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Json {

    public static void main(String[] args) {
        ApplicationData data = new ApplicationData();
        data.setCurrentDomain("server1.godswila.guru");
        data.setSaltSizeInBytes(16);
        data.setMulticastAddress("224.1.1.255");
        data.setMulticastPort(23845);
        data.setUnicastPort(23846);
        data.setRelayPort(23847);
        data.setNetworkInterface("eth12");
        data.setBase64AES("P3FXqAUgfhy5cTjYdWlPQBJ/d6fdpbR88YsDPWPbo14=");
        data.setTls(true);

        List<Utilisateur> users = new ArrayList<>();
        Utilisateur user1 = new Utilisateur();
        user1.setLogin("lswinnen");
        user1.setBcryptHash("tDtRfkGpCO5kuMqsPrda5RWRd3/j0Va");
        user1.setBcryptRound(14);
        user1.setBcryptSalt("cD2UrFc62QNG5d5ogBQXTO");
        List<String> followers1 = new ArrayList<>();
        followers1.add("louis@server1.godswila.guru");
        followers1.add("swilabus@server2.godswila.guru");
        user1.setFollowers(followers1);
        List<String> userTags1 = new ArrayList<>();
        userTags1.add("#tendance123@server1.godswila.guru");
        user1.setUserTags(userTags1);
        user1.setLockoutCounter(0);
        users.add(user1);

        Utilisateur user2 = new Utilisateur();
        user2.setLogin("louis");
        user2.setBcryptHash("p9EhYRnBripLCKGieLHXicOSU35jqUS");
        user2.setBcryptRound(14);
        user2.setBcryptSalt("Xy7qayjAB0YTIXTzdEUV2u");
        List<String> user = new ArrayList<>();
        List<String> followers2 = new ArrayList<>();
        List<String> userTags2 = new ArrayList<>();
        userTags2.add("#tendance123@server1.godswila.guru");
        userTags2.add("#tendance456@server2.godswila.guru");
        user2.setFollowers(followers2);
        user2.setUserTags(userTags2);
        user2.setLockoutCounter(0);
        users.add(user2);

        data.setUsers(users);

        List<Tag> tags = new ArrayList<>();
        Tag tag1 = new Tag();
        tag1.setTag("#tendance123");
        List<String> followers3 = new ArrayList<>();
        followers3.add("louis@server1.godswila.guru");
        followers3.add("lswinnen@server1.godswila.guru");
        tag1.setFollowers(followers3);
        tags.add(tag1);

        data.setTags(tags);

        String path = "src/main/java/org/model/test.json";
        sauvegarder(data, path);
        List<Utilisateur> listeUser = getListeUser(path);

        System.out.println("Voici le pseudo de l'utilisateur 1: " + getUser(path, "lswinnen").getLogin());
    }

    public static void sauvegarder(ApplicationData data, String path) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        File file = new File(path);

        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        try (FileWriter writer = new FileWriter(path)) {
            gson.toJson(data, writer);
        } catch (JsonIOException | IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Utilisateur> getListeUser(String path) {
        Gson gson = new Gson();
        List<Utilisateur> users = new ArrayList<>();

        try (Reader reader = new FileReader(path)) {
            ApplicationData data = gson.fromJson(reader, ApplicationData.class);
            users = data.getUsers();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return users;
    }

    public static Utilisateur getUser(String path, String login) {
        Gson gson = new Gson();
        Utilisateur user = new Utilisateur();

        try (Reader reader = new FileReader(path)) {
            ApplicationData data = gson.fromJson(reader, ApplicationData.class);
            List<Utilisateur> users = data.getUsers();
            for (Utilisateur u : users) {
                if (u.getLogin().equals(login)) {
                    user = u;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return user;
    }

}

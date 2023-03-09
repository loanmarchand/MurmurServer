package org.MurmurServer.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import org.MurmurServer.model.ApplicationData;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Cette classe permet de sauvegarder et de charger les données de l'application en manipulant un objet ApplicationData.
 * et en utilisant la librairie Gson.
 */
public class Json {
    public static final String URL_CONFIG = "src/main/resources/config.json";
    private final String URL_JSON = "src/main/resources/data.json";

    /**
     * Sauvegarde les données de l'application dans un fichier JSON.
     *
     * @param applicationData : les données de l'application.
     */
    public void sauvegarder(ApplicationData applicationData) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        File file = new File(URL_JSON);

        if (creerFichierSiInexistant(file)) {
            try (FileWriter writer = new FileWriter(URL_JSON)) {
                gson.toJson(applicationData, writer);
            } catch (JsonIOException | IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Cette méthode va tenter de créer le fichier s'il n'existe pas.
     *
     * @param file : le fichier à créer.
     * @return : true si le fichier a été créé ou si le fichier existe déjà, false sinon.
     */
    private boolean creerFichierSiInexistant(File file) {
        // Tester si file existe
        if (!file.exists()) {
            try {
                if (file.createNewFile()) {
                    return true;
                }
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }

    /**
     * Récupérer un objet Utilisateur sur base de son login.
     *
     * @param login : le login de l'utilisateur à récupérer.
     * @return : l'utilisateur ou null si l'utilisateur n'existe pas.
     */
    public Utilisateur getUser(String login) {
        Gson gson = new Gson();
        Utilisateur user = new Utilisateur();

        if (creerFichierSiInexistant(new File(URL_JSON))) {
            try (Reader reader = new FileReader(URL_JSON)) {
                ApplicationData data = gson.fromJson(reader, ApplicationData.class);
                List<Utilisateur> users = data.getUsers();
                for (Utilisateur u : users) {
                    if (u.getLogin().equals(login)) {
                        user = u;
                    }
                }
                return user;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Récupérer un objet ApplicationData.
     *
     * @return : les données de l'application ou null si le fichier n'existe pas.
     */
    public ApplicationData getApplicationData() {
        Gson gson = new Gson();
        if (creerFichierSiInexistant(new File(URL_JSON))) {
            try (Reader reader = new FileReader(URL_JSON)) {
                return gson.fromJson(reader, ApplicationData.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Vérifier si le fichier existe.
        File file = new File(URL_JSON);
        if (!file.exists()) {
            sauvegarder(getApplicationDataVide());
        }

        try (Reader reader = new FileReader(URL_JSON)) {
            return gson.fromJson(reader, ApplicationData.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Renvoie un objet ApplicationData avec des valeurs par défaut.
     *
     * @return : les données de l'application
     */
    private ApplicationData getApplicationDataVide() {
        Gson gson = new Gson();
        ApplicationData data = new ApplicationData();
        try (Reader reader = new FileReader(URL_CONFIG)) {
            data = gson.fromJson(reader, ApplicationData.class);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Utilisateur> users = new ArrayList<>();
        data.setUsers(users);
        List<Tag> tags = new ArrayList<>();
        data.setTags(tags);
        return data;
    }
}

package java.model;

import java.io.*;
import java.io.IOException;
import org.json.JSONObject;


public class Json {

    public static void main(String[] args) {
        System.out.println("Début du programme Json");
        System.out.println("Création d'un utilisateur");
        Utilisateur utilisateur = new Utilisateur("test", 1, "sel", "hash");
        System.out.println("Ajout de l'utilisateur dans le fichier json");
        Json json = new Json();
        if (json.ajouterUtilisateur(utilisateur)) {
            System.out.println("Utilisateur ajouté avec succès");
        } else {
            System.out.println("Erreur lors de l'ajout de l'utilisateur");
        }

        System.out.println("Fin du programme Json");
    }
    public boolean ajouterUtilisateur(Utilisateur utilisateur) {
        String nom_fichier = "src\\java.model\\liste_utilisateurs.json";
        File fichier = new File(nom_fichier);

        if (!fichier.exists()) {
            try {
                fichier.createNewFile();

            } catch (IOException e) {
                return false;
            }
        }

        return true;
    }
}

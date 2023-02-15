package org.model;

public class Utilisateur {
    private final String nom;
    private final int nbrRotation;
    private final String sel;
    private final String hash;

    public Utilisateur(String nom, int nbrRotation, String sel, String hash) {
        this.nom = nom;
        this.nbrRotation = nbrRotation;
        this.sel = sel;
        this.hash = hash;
    }
}

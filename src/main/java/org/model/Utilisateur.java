package org.model;

import java.util.List;

public class Utilisateur {
    private String login;
    private String bcryptHash;
    private int bcryptRound;
    private String bcryptSalt;
    private List<String> followers;
    private List<String> userTags;
    private int lockoutCounter;

    public Utilisateur() {
    }

    public Utilisateur(String login, String bcryptHash, int bcryptRound, String bcryptSalt, List<String> followers,
                       List<String> userTags, int lockoutCounter) {
        this.login = login;
        this.bcryptHash = bcryptHash;
        this.bcryptRound = bcryptRound;
        this.bcryptSalt = bcryptSalt;
        this.followers = followers;
        this.userTags = userTags;
        this.lockoutCounter = lockoutCounter;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getBcryptHash() {
        return bcryptHash;
    }

    public void setBcryptHash(String bcryptHash) {
        this.bcryptHash = bcryptHash;
    }

    public int getBcryptRound() {
        return bcryptRound;
    }

    public void setBcryptRound(int bcryptRound) {
        this.bcryptRound = bcryptRound;
    }

    public String getBcryptSalt() {
        return bcryptSalt;
    }

    public void setBcryptSalt(String bcryptSalt) {
        this.bcryptSalt = bcryptSalt;
    }

    public List<String> getFollowers() {
        return followers;
    }

    public void setFollowers(List<String> followers) {
        this.followers = followers;
    }

    public List<String> getUserTags() {
        return userTags;
    }

    public void setUserTags(List<String> userTags) {
        this.userTags = userTags;
    }

    public int getLockoutCounter() {
        return lockoutCounter;
    }

    public void setLockoutCounter(int lockoutCounter) {
        this.lockoutCounter = lockoutCounter;
    }
}

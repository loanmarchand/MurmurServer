package org.MurmurServer.model;

import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Utilisateur that = (Utilisateur) o;
        return bcryptRound == that.bcryptRound && lockoutCounter == that.lockoutCounter && Objects.equals(login, that.login) && Objects.equals(bcryptHash, that.bcryptHash) && Objects.equals(bcryptSalt, that.bcryptSalt) && Objects.equals(followers, that.followers) && Objects.equals(userTags, that.userTags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login, bcryptHash, bcryptRound, bcryptSalt, followers, userTags, lockoutCounter);
    }
}

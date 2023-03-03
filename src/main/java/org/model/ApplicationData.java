package org.model;

import java.util.List;

public class ApplicationData {
    private String currentDomain;
    private int saltSizeInBytes;
    private String multicastAddress;
    private int multicastPort;
    private int unicastPort;
    private int relayPort;
    private String networkInterface;
    private String base64AES;
    private boolean tls;
    private List<Utilisateur> users;
    private List<Tag> tags;

    public ApplicationData(String currentDomain, int saltSizeInBytes, String multicastAddress, int multicastPort,
                           int unicastPort, int relayPort, String networkInterface, String base64AES,
                           boolean tls, List<Utilisateur> users, List<Tag> tags) {
        this.currentDomain = currentDomain;
        this.saltSizeInBytes = saltSizeInBytes;
        this.multicastAddress = multicastAddress;
        this.multicastPort = multicastPort;
        this.unicastPort = unicastPort;
        this.relayPort = relayPort;
        this.networkInterface = networkInterface;
        this.base64AES = base64AES;
        this.tls = tls;
        this.users = users;
        this.tags = tags;
    }

    public ApplicationData() {
    }

    public String getCurrentDomain() {
        return currentDomain;
    }

    public void setCurrentDomain(String currentDomain) {
        this.currentDomain = currentDomain;
    }

    public int getSaltSizeInBytes() {
        return saltSizeInBytes;
    }

    public void setSaltSizeInBytes(int saltSizeInBytes) {
        this.saltSizeInBytes = saltSizeInBytes;
    }

    public String getMulticastAddress() {
        return multicastAddress;
    }

    public void setMulticastAddress(String multicastAddress) {
        this.multicastAddress = multicastAddress;
    }

    public int getMulticastPort() {
        return multicastPort;
    }

    public void setMulticastPort(int multicastPort) {
        this.multicastPort = multicastPort;
    }

    public int getUnicastPort() {
        return unicastPort;
    }

    public void setUnicastPort(int unicastPort) {
        this.unicastPort = unicastPort;
    }

    public int getRelayPort() {
        return relayPort;
    }

    public void setRelayPort(int relayPort) {
        this.relayPort = relayPort;
    }

    public String getNetworkInterface() {
        return networkInterface;
    }

    public void setNetworkInterface(String networkInterface) {
        this.networkInterface = networkInterface;
    }

    public String getBase64AES() {
        return base64AES;
    }

    public void setBase64AES(String base64AES) {
        this.base64AES = base64AES;
    }

    public boolean isTls() {
        return tls;
    }

    public void setTls(boolean tls) {
        this.tls = tls;
    }

    public List<Utilisateur> getUsers() {
        return users;
    }
    public Utilisateur getUser(String login) {
        for (Utilisateur user : users) {
            if (user.getLogin().equals(login)) {
                return user;
            }
        }
        return null;
    }

    public void setUsers(List<Utilisateur> users) {
        this.users = users;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public void setTags(List<Tag> tags) {
        this.tags = tags;
    }
}




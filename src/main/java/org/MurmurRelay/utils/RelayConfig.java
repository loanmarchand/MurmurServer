package org.MurmurRelay.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RelayConfig {

    private Map<String, String> aesToServerMap;
    private final String configFile;

    public RelayConfig(String configFile) {
        this.aesToServerMap = new HashMap<>();
        this.configFile = configFile;
        loadConfig();
    }

    private void loadConfig() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8));
            Gson gson = new Gson();
            this.aesToServerMap = gson.fromJson(reader, HashMap.class);
        } catch (FileNotFoundException e) {
            System.out.println("Could not find config file: " + configFile);
        }
    }

    public void addEntry(String aesKey, String serverAddress) {
        this.aesToServerMap.put(aesKey, serverAddress);
        saveConfig();
    }

    private void saveConfig() {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileWriter writer = new FileWriter(configFile);
            gson.toJson(this.aesToServerMap, writer);
            writer.close();
        } catch (IOException e) {
            System.out.println("Error saving config file: " + configFile);
        }
    }

    public Map<String, String> getAesToServerMap() {
        return aesToServerMap;
    }

    public void setAesToServerMap(Map<String, String> aesToServerMap) {
        this.aesToServerMap = aesToServerMap;
    }

    public static void main(String[] args) {
        RelayConfig config = new RelayConfig("src/main/resources/relayConfig.json");
        config.addEntry("test", "test");
        System.out.println(config.getAesToServerMap());
        System.out.println(config.getKeys()[1]);
    }

    public String[] getKeys() {
        return aesToServerMap.keySet().toArray(new String[0]);
    }
}

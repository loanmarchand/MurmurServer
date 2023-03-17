package org.MurmurRelay.utils;

import com.google.gson.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RelayConfig {

    private HashMap<String, String> domainKeyMap;
    private final String configFile;
    private String currentDomain;
    private String networkInterface;

    public RelayConfig(String configFile) {
        this.configFile = configFile;
        loadConfig();
    }

    private void loadConfig() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(configFile), StandardCharsets.UTF_8));
            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
            this.currentDomain = jsonObject.get("currentDomain").getAsString();
            this.networkInterface = jsonObject.get("networkInterface").getAsString();
            JsonArray domainKeyArray = jsonObject.getAsJsonArray("domainKey");
            this.domainKeyMap = new HashMap<>();
            for (JsonElement element : domainKeyArray) {
                JsonObject domainKeyObject = element.getAsJsonObject();
                for (String key : domainKeyObject.keySet()) {
                    domainKeyMap.put(key, domainKeyObject.get(key).getAsString());
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Impossible de trouver le fichier de configuration : " + configFile);
        }
    }

    public String getCurrentDomain() {
        return currentDomain;
    }

    public HashMap<String, String> getDomainKeyMap() {
        return domainKeyMap;
    }

    public String getNetworkInterface() {
        return networkInterface;
    }

    public void setNetworkInterface(String networkInterface) {
        this.networkInterface = networkInterface;
        //Sauvegarder dans le fichier de config
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8));
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("currentDomain", currentDomain);
            jsonObject.addProperty("networkInterface", networkInterface);
            JsonArray domainKeyArray = new JsonArray();
            for (Map.Entry<String, String> entry : domainKeyMap.entrySet()) {
                JsonObject domainKeyObject = new JsonObject();
                domainKeyObject.addProperty(entry.getKey(), entry.getValue());
                domainKeyArray.add(domainKeyObject);
            }
            jsonObject.add("domainKey", domainKeyArray);
            gson.toJson(jsonObject, writer);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

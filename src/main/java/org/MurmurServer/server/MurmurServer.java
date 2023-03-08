package org.MurmurServer.server;

import org.MurmurRelay.utils.AesUtils;
import org.MurmurServer.model.ApplicationData;
import org.MurmurServer.model.Json;
import org.MurmurServer.model.Utilisateur;

import javax.crypto.SecretKey;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MurmurServer {
    private static final int DEFAULT_PORT = 22510;
    private final List<ClientRunnable> clientList;
    private final ExecutorService executorService;

    private final AesUtils aesUtils;
    public MurmurServer(int port) throws IOException {
        aesUtils = new AesUtils();
        clientList = Collections.synchronizedList(new ArrayList<>());
        SSLServerSocket server;
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        System.setProperty("javax.net.ssl.keyStore", "src/main/resources/star.godswila.guru.p12");
        System.setProperty("javax.net.ssl.keyStorePassword", "labo2023");
        SSLServerSocketFactory serverScocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        server = (SSLServerSocket) serverScocketFactory.createServerSocket(port);


        while (true) {
            sendEchoToRelay();
            SSLSocket client = (SSLSocket) server.accept();
            ClientRunnable runnable = new ClientRunnable(client, this);
            clientList.add(runnable);
            executorService.execute(runnable);

        }
    }

    private void sendEchoToRelay() {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try(DatagramSocket socket = new DatagramSocket()){
                InetAddress address = InetAddress.getByName("224.1.1.255");
                ApplicationData applicationData = Json.getApplicationData();
                assert applicationData != null;
                String message = "ECHO " + applicationData.getCurrentDomain();
                byte[] buffer = message.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, DEFAULT_PORT);
                //TODO: mettre en place le system de chiffrement AES-256 GCM
                System.out.println(applicationData.getBase64AES());
                //Si la cle n'est pas encore enregistre on la genere
                if (Objects.equals(applicationData.getBase64AES(), "")) {
                    SecretKey secretKey = aesUtils.generateKey();
                    System.out.println("Secret key: " + secretKey.toString());
                    applicationData.setBase64AES(aesUtils.encodeKey(secretKey));
                    Json.sauvegarder(applicationData);
                }
                //Chiffrement du message
                //buffer = aesUtils.encrypt(message, aesUtils.decodeKey(applicationData.getBase64AES()));
                packet.setData(buffer);
                socket.send(packet);
                //Decryption du message
                //String decrypted = aesUtils.decrypt(buffer, aesUtils.decodeKey(applicationData.getBase64AES()));
                System.out.println("Decrypted: " + message);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 15, TimeUnit.SECONDS);
    }

    public void broadcastToAllClientsExceptMe(List<Utilisateur> me, String message, ClientRunnable clientRunnable) {
        clientList.stream()
                .filter(client -> me.contains(client.getUser()) && client != clientRunnable)
                .forEach(client -> client.sendMessage(message));
    }

    public static void main(String[] args) throws IOException {
        MurmurServer murmurServer = new MurmurServer(DEFAULT_PORT);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            murmurServer.executorService.shutdown();
        }));
    }
}
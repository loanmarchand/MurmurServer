package org.MurmurServer.server;

import org.MurmurRelay.relay.MurmurRelay;
import org.MurmurServer.model.ApplicationData;
import org.MurmurServer.model.Json;
import org.MurmurServer.model.Protocol;
import org.MurmurServer.model.Utilisateur;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MurmurServer {
    private static final int DEFAULT_PORT = 23505;

    private final List<ClientRunnable> clientList;
    private final ExecutorService executorService;
    private final Json json;
    private final Protocol protocol;

    public MurmurServer(int port) throws IOException {
        clientList = Collections.synchronizedList(new ArrayList<>());
        SSLServerSocket server;
        json = new Json();
        protocol = new Protocol();
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


    /**
     * Envoie un echo en multicast toutes les 15 secondes
     * A l'adresse 224.1.1.255
     */
    private void sendEchoToRelay() {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try(DatagramSocket socket = new DatagramSocket()){
                InetAddress address = InetAddress.getByName("224.1.1.255");
                ApplicationData applicationData = json.getApplicationData();
                assert applicationData != null;
                String message = protocol.build_echo(applicationData.getCurrentDomain(),DEFAULT_PORT);
                byte[] buffer = message.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, DEFAULT_PORT);
                socket.send(packet);
                System.out.println("Message envoyé : " + message);

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
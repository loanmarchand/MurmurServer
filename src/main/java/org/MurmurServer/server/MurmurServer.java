package org.MurmurServer.server;

import org.MurmurRelay.relay.ServerListener;
import org.MurmurServer.model.ApplicationData;
import org.MurmurServer.model.Json;
import org.MurmurServer.model.Protocol;
import org.MurmurServer.model.Utilisateur;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MurmurServer {
    private static final int DEFAULT_PORT = 23505;
    private static final int DEFAULT_RELAY_PORT = 23515;

    private final List<ClientRunnable> clientList;
    private final ExecutorService executorService;
    private final Json json;
    private final Protocol protocol;
    ServerSocket serverSocket;
    SSLSocket client;
    Socket relayClient;
    SSLServerSocket server;

    /**
     * Le constructeur de la classe MurmurServer.
     * Il initialise les serveur et écoute les connexions entrantes des clients et du relay.
     * @param port Le numéro de port sur lequel le serveur écoutera les connexions entrantes.
     *
     * @throws IOException Si une erreur se produit lors de la créations du serveur.
     */
    public MurmurServer(int port) throws IOException {
        // Initialise la liste des clients et le pool de threads pour gérer les clients connectés.
        clientList = Collections.synchronizedList(new ArrayList<>());

        // Initialise les objets Json et Protocol pour gérer les messages et la communication.
        json = new Json();
        protocol = new Protocol();

        // Créer un pool de threads pour gérer les connexion du clients.
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        // Configure les propriétés SSL pour le serveur.
        System.setProperty("javax.net.ssl.keyStore", "src/main/resources/star.godswila.guru.p12");
        System.setProperty("javax.net.ssl.keyStorePassword", "labo2023");

        // Créer un serveur SSL.
        SSLServerSocketFactory serverScocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        server = (SSLServerSocket) serverScocketFactory.createServerSocket(port);

        // Créer un ServeurSocket pour gérer les connexion relay
        serverSocket = new ServerSocket(DEFAULT_RELAY_PORT);

        // Envoie un message Echo au relay.
        sendEchoToRelay();


        // Boucle infinie pour accepter les connexions entrantes des clients.
        while (true) {

             new Thread(()->{
                 try {
                     startClientConnexion();
                 }catch (Exception e){
                     e.printStackTrace();
                    }
             }).start();

            new Thread(()->{
                try {
                    startRelayConnexion();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
    }


    /**
     * Lance un thread qui écoute les connexions entrantes du relay
     * @throws IOException
     */
    private void startRelayConnexion() throws IOException {
        relayClient = serverSocket.accept();
        ServerListener serverListener = new ServerListener(relayClient, this);
        executorService.execute(serverListener);
    }


    /**
     * Envoie un echo en multicast toutes les 15 secondes
     * A l'adresse 224.1.1.255
     */
    private void sendEchoToRelay() {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try(MulticastSocket socket = new MulticastSocket()){
                socket.setNetworkInterface(NetworkInterface.getByName("wlan2"));
                InetAddress address = InetAddress.getByName("224.1.1.255");
                socket.joinGroup(address);
                ApplicationData applicationData = json.getApplicationData();
                assert applicationData != null;
                String message = protocol.build_echo(applicationData.getCurrentDomain(),DEFAULT_RELAY_PORT);
                byte[] buffer = message.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, DEFAULT_RELAY_PORT);

                if(relayClient != null){
                    scheduledExecutorService.shutdown();
                    System.out.println("Arrêt de l'envoi de messages à Relay.");
                } else {
                    socket.send(packet);
                    System.out.println("Message envoyé : " + message);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 15, TimeUnit.SECONDS);
    }



    public void broadcastToAllClients(List<String> me, String message) {
        for (ClientRunnable client : clientList) {
            if (!me.contains(client.getUser().getLogin()+"@"+ json.getApplicationData().getCurrentDomain())) {
                client.sendMessage(message);
                System.out.println("Message envoyé à " + client.getUser().getLogin());
            }
        }

    }

    public static void main(String[] args) throws IOException {
        MurmurServer murmurServer = new MurmurServer(DEFAULT_PORT);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            murmurServer.executorService.shutdown();
        }));
    }

    public String getSecretKey() {
        return json.getApplicationData().getBase64AES();
    }

    public void sendToRelay(String ligne) {
        try {
            PrintWriter out = new PrintWriter(new OutputStreamWriter(relayClient.getOutputStream(), StandardCharsets.UTF_8), true);
            out.println(ligne);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void startClientConnexion() throws IOException {
        client= (SSLSocket) server.accept();
        ClientRunnable runnable = new ClientRunnable(client, this);
        clientList.add(runnable);
        executorService.execute(runnable);
    }
    }
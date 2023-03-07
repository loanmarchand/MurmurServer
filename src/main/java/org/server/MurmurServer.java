package org.server;

import org.model.Utilisateur;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MurmurServer {
    private static final int DEFAULT_PORT = 22510;
    private final List<ClientRunnable> clientList;
    ExecutorService executorService;

    public MurmurServer(int port) {
        clientList = Collections.synchronizedList(new ArrayList<>());
        SSLServerSocket server;
        SSLSocket client;
        executorService = Executors.newFixedThreadPool(10);
        try {
            //recupere le certificat
            System.setProperty("javax.net.ssl.keyStore", "src/main/resources/star.godswila.guru.p12");
            System.setProperty("javax.net.ssl.keyStorePassword", "labo2023");
            SSLServerSocketFactory serverScocketFactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            server = (SSLServerSocket) serverScocketFactory.createServerSocket(port);
            while(true) {
                client = (SSLSocket) server.accept();
                ClientRunnable runnable = new ClientRunnable(client, this);
                clientList.add(runnable);
                executorService.execute(runnable);
                //Toutes les 15 secondes, on envoye ECHO sur l'adresse multicast 224.1.1.255 sur le port 22510

                new Thread(() -> {
                    while(true) {
                        try {
                            Thread.sleep(15000);

                        } catch(InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

        } catch(IOException ex) {
            ex.printStackTrace();
        }

    }

    public void broadcastToAllClientsExceptMe(List<Utilisateur> me, String message, ClientRunnable clientRunnable) {
        //envoyer le message à tous les utilisateurs sauf moi
        for(ClientRunnable client : clientList) {
            if(me.contains(client.getUser()) && client != clientRunnable) {
                client.sendMessage(message);
            }
        }

    }

    public static void main(String[] args) {
        MurmurServer murmurServer= new MurmurServer(DEFAULT_PORT);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down server...");
            murmurServer.executorService.shutdown();
        }));
    }
}

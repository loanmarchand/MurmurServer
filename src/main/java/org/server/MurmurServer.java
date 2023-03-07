package org.server;

import org.model.Utilisateur;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MurmurServer {
    private static final int DEFAULT_PORT = 22510;
    private List<ClientRunnable> clientList;

    public MurmurServer(int port) {
        clientList = Collections.synchronizedList(new ArrayList<>());
        ServerSocket server;
        Socket client;
        try {
            server = new ServerSocket(port);
            while(true) {
                client= server.accept();
                ClientRunnable runnable = new ClientRunnable(client, this);
                clientList.add(runnable);
                (new Thread(runnable)).start();
            }

        } catch(IOException ex) {
            ex.printStackTrace();
        }

    }

    public void broadcastToAllClientsExceptMe(List<Utilisateur> me, String message, ClientRunnable clientRunnable) {
        //envoyer le message à tous les utilisateurs sauf moi
        for(ClientRunnable client : clientList) {
            //affiche client
            if(me.contains(client.getUser()) && client != clientRunnable) {
                client.sendMessage(message);
            }
        }

    }

    public static void main(String[] args) {
        new MurmurServer(DEFAULT_PORT);
    }
}

package org.MurmurRelay.relay;

import org.MurmurRelay.utils.AesUtils;
import org.MurmurServer.model.CommandServer;
import org.MurmurServer.model.Protocol;
import org.MurmurServer.server.MurmurServer;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerListener implements Runnable{
    private final Socket relayClient;
    private final MurmurServer murmurServer;
    private BufferedReader in;
    private PrintWriter out;
    private AesUtils aesUtils;
    private CommandServer commandServer;

    public ServerListener(Socket relayClient, MurmurServer murmurServer) {
        this.relayClient = relayClient;
        this.murmurServer = murmurServer;
        aesUtils = new AesUtils();
        try {
            in = new BufferedReader(new InputStreamReader(relayClient.getInputStream(), StandardCharsets.UTF_8));
            out = new PrintWriter(new OutputStreamWriter(relayClient.getOutputStream(), StandardCharsets.UTF_8), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        Protocol protocol = new Protocol();
        try {
            String message = in.readLine();
            System.out.println(message);
            while (message!=null){
                String decrypt = aesUtils.decrypt(message, murmurServer.getSecretKey());
                System.out.println(decrypt);
                Pattern pattern = Pattern.compile(protocol.getRxSend());
                Matcher matcher = pattern.matcher(decrypt);
                if (matcher.find()) {
                    String ligne = matcher.group(10)+" "+matcher.group(13);
                    String user = matcher.group(11);
                    String domain = matcher.group(2);
                    if (ligne.matches(protocol.getRxFollow())){
                        pattern = Pattern.compile(protocol.getRxFollow());
                        matcher = pattern.matcher(ligne);
                        commandServer = new CommandServer();
                        commandServer.followTagRelay(matcher.group(1),user,domain);
                    }
                    // Gestion des messages
                    if (ligne.matches(protocol.getRxMessage())){
                        commandServer = new CommandServer();
                        // commandServer.sendMsg(ligne);//TODO : Envoyer le message au serveur + DEMANDER id
                    }
                }



            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }
}

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
                Pattern pattern = Pattern.compile(protocol.getRxSend());//TODO : CHANGER ICI
                Matcher matcher = pattern.matcher(decrypt);
                if (matcher.find()) {
                    String ligneFollow = matcher.group(10)+" "+matcher.group(11)+"@"+matcher.group(2);
                    String ligneMsg = matcher.group(9);
                    if (ligneFollow.matches(protocol.getRxFollow())){
                        String user = matcher.group(12);
                        String domain = matcher.group(4);
                        Pattern pattern1 = Pattern.compile(protocol.getRxFollow());
                        Matcher matcher1 = pattern1.matcher(ligneFollow);
                        //Afficher tous les groupes
                        if (matcher1.find()) {
                            try {
                                String tag = matcher1.group(2);
                                if (tag.equals("#")){
                                    commandServer = new CommandServer();
                                    commandServer.followTagRelay(matcher1.group(1),user+"@"+domain);
                                    System.out.println("Follow TAG");
                                }
                                else {
                                    commandServer = new CommandServer();
                                    commandServer.sendFollowUser(ligneFollow,user);
                                    System.out.println("Follow USER");
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                        }
                    }
                    // Gestion des messages
                    if (ligneMsg.matches(protocol.geRxMsgs())){
                        System.out.println("Message");
                        String userWhoSend = matcher.group(11)+"@"+matcher.group(2);
                        String userWhoReceive = matcher.group(12)+"@"+matcher.group(4);
                        String messageToSend = matcher.group(13);

                        commandServer = new CommandServer();
                        commandServer.sendMsgRelay(userWhoSend,userWhoReceive,messageToSend, murmurServer);
                    }
                }

                message = in.readLine();

            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }
}

package org.MurmurRelay.relay;

import org.MurmurRelay.utils.AesUtils;
import org.MurmurRelay.utils.RelayConfig;
import org.MurmurServer.model.Json;
import org.MurmurServer.model.Protocol;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MurmurRelay {
    private final int relayPort;
    private final Protocol protocol;
    private final Json json;
    private final HashMap<String, String> domainKeyMap;
    private final Map<String, Socket> connectedServers;
    private AesUtils aesUtils;
    private RelayConfig relayConfig;

    public MurmurRelay(int relayPort) throws IOException {
        this.relayPort = relayPort;
        this.protocol = new Protocol();
        this.json = new Json();
        this.aesUtils = new AesUtils();
        this.relayConfig = new RelayConfig("src/main/resources/configRelay.json");
        this.domainKeyMap =  relayConfig.getDomainKeyMap();
        this.connectedServers = new HashMap<>();

        listenForMulticastAnnouncements();
    }

    private void listenForMulticastAnnouncements() throws IOException {
        try (MulticastSocket multicastSocket = new MulticastSocket(relayPort)) {
            InetAddress groupAddress = InetAddress.getByName("224.1.1.255");
            multicastSocket.setNetworkInterface(NetworkInterface.getByName("eth5"));
            multicastSocket.joinGroup(groupAddress);

            while (true) {
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(packet);

                String message = new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
                System.out.println("Received message: " + message);

                Pattern pattern = Pattern.compile(protocol.getRxEcho());
                Matcher matcher = pattern.matcher(message);
                if (matcher.find()){
                    String domain = matcher.group(1);
                    int serverPort = Integer.parseInt(matcher.group(2));
                    System.out.println("Received echo from " + domain + " at " + packet.getAddress() + ":" + serverPort);
                    if (domainKeyMap.containsKey(domain) && !connectedServers.containsKey(domain)) {
                        Socket serverSocket = new Socket(packet.getAddress(), serverPort);
                        connectedServers.put(domain, serverSocket);
                        System.out.println("Connected to " + domain + " at " + packet.getAddress() + ":" + serverPort);
// Démarrer un thread pour gérer la communication avec le serveur
                        new Thread(() -> handleServerCommunication(serverSocket, domain)).start();
                    }
                }
            }
        }
    }

    private void handleServerCommunication(Socket serverSocket, String domain) {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(serverSocket.getOutputStream()))) {

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received message from " + domain + ": " + inputLine);
                System.out.println(domainKeyMap.get(domain));
                String decryptedMessage = aesUtils.decrypt(inputLine, domainKeyMap.get(domain));
                String destinationDomain = protocol.getDestinationDomainFromMessage(decryptedMessage);
                //SEND 1234 server1.godswila.guru server2.godswila.guru MSGS thibaut@server2.godswila.guru hjkhjk
                System.out.println("Received message from " + domain + " for " + destinationDomain + ": " + decryptedMessage);

                if (connectedServers.containsKey(destinationDomain)) {
                    String encryptedMessage = aesUtils.encrypt(decryptedMessage, domainKeyMap.get(destinationDomain));
                    BufferedWriter destinationServerOut = new BufferedWriter(new OutputStreamWriter(connectedServers.get(destinationDomain).getOutputStream()));
                    destinationServerOut.write(encryptedMessage);
                    destinationServerOut.newLine();
                    destinationServerOut.flush();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            connectedServers.remove(domain);
        }
    }

    public static void main(String[] args) throws IOException {
        int relayPort = 23515;
        new MurmurRelay(relayPort);
    }
}

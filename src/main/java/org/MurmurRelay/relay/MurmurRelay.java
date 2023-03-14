package org.MurmurRelay.relay;

import org.MurmurRelay.utils.AesUtils;
import org.MurmurRelay.utils.RelayConfig;
import org.MurmurServer.model.ApplicationData;
import org.MurmurServer.model.Json;
import org.MurmurServer.model.Protocol;

import javax.crypto.SecretKey;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MurmurRelay {
    private final Map<String, Socket> serverMap;
    private final Protocol protocol;
    private AesUtils aesUtils;
    private RelayConfig relayConfig;

    public MurmurRelay() {
        serverMap = new HashMap<>();
        protocol = new Protocol();
        aesUtils = new AesUtils();
        relayConfig = new RelayConfig("src/main/resources/relayConfig.json");
    }

    public void start() throws IOException {
        InetAddress group = InetAddress.getByName("224.1.1.255");
        MulticastSocket socket = new MulticastSocket(23505);
        socket.joinGroup(group);

        while (true) {
            byte[] buffer = new byte[15000];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);

            String message = new String(packet.getData());
            String domain = getDomain(message)[0];
            String port = getDomain(message)[1];
                if (relayConfig.getDomains().contains(domain)&&serverMap.get(domain)==null) {
                    Socket serverSocket = new Socket(domain, Integer.parseInt(port));
                    serverMap.put(domain, serverSocket);
                    System.out.println("Server " + domain + " connected.");

                    Thread thread = new Thread(new ServerListener(serverSocket, this));
                    thread.start();
                }

        }
    }

    public String[] getDomain(String message) {
        Pattern pattern = Pattern.compile(protocol.getRxEcho());
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return new String[]{matcher.group(1), matcher.group(2)};
        }
        return null;
    }

    public void sendMessage(String domain,String message) throws Exception {
        if (serverMap.get(domain)==null){
            return;
        }
        Socket socket = serverMap.get(domain);
        String cryptedMessage = aesUtils.encrypt(message,getAesKey(socket));
        socket.getOutputStream().write(cryptedMessage.getBytes());
        socket.getOutputStream().flush();

    }

    public static void main(String[] args) throws IOException {
        MurmurRelay relay = new MurmurRelay();
        relay.start();
    }

    public SecretKey getAesKey(Socket serverSocket) {
        String domain = serverSocket.getInetAddress().getHostName();
        return aesUtils.decodeKey(relayConfig.getKey(domain));
    }
}
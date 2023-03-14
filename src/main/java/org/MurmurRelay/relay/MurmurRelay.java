package org.MurmurRelay.relay;

import org.MurmurRelay.utils.AesUtils;
import org.MurmurRelay.utils.RelayConfig;
import org.MurmurServer.model.Protocol;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MurmurRelay implements Runnable{

    private static final int DEFAULT_PORT = 23505;
    private final RelayConfig relayConfig;
    private final Protocol protocol;

    private final Map<String, SSLSocket> connectedDomains;
    private BufferedReader in;
    private PrintWriter out;
    private AesUtils aesUtils;


    public MurmurRelay() {
        protocol = new Protocol();
        aesUtils = new AesUtils();
        connectedDomains = Collections.synchronizedMap(new HashMap<>());
        relayConfig = new RelayConfig("src/main/resources/relayConfig.json");
    }

    public void start() throws IOException {
        String MULTICAST_ADDRESS = "224.1.1.255";
        InetAddress group = InetAddress.getByName(MULTICAST_ADDRESS);
        MulticastSocket multicastSocket = new MulticastSocket(DEFAULT_PORT);
        multicastSocket.joinGroup(group);

        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        while (true){
            multicastSocket.receive(packet);
            String message = new String(packet.getData(),packet.getOffset(),packet.getLength(),"UTF-8");
            String domain = getDomain(message);
            if(domain != null&& IsNotConnected(domain)){
                System.out.printf("Domain %s found%n",domain);
                //TODO : Recuperer l'adresse ip a partir du domaine puis se connecter en TCP unicast au serveur correspondant
                InetAddress addres = InetAddress.getByName(domain);
                SSLSocket relaySocket = (SSLSocket) SSLSocketFactory.getDefault().createSocket(addres,DEFAULT_PORT);
                connectedDomains.put(domain,relaySocket);

            }


        }
    }

    private boolean IsNotConnected(String domain) {
        return !connectedDomains.containsKey(domain);
    }

    private String getDomain(String message) {
            Pattern pattern = Pattern.compile(protocol.getRxEcho());
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()){
                String goodMessage = matcher.group(1);
                //Tester si le messaga est un domain compris dans la liste
                if (relayConfig.getDomains().contains(goodMessage)){
                    return goodMessage;
                }
        }
        return null;
    }

    public static void main(String[] args) {
        MurmurRelay murmurRelay = new MurmurRelay();
        try {
            murmurRelay.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
    }

}

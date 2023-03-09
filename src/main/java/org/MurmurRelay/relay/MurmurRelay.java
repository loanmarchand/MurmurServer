package org.MurmurRelay.relay;

import org.MurmurRelay.utils.RelayConfig;
import org.MurmurServer.model.Protocol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MurmurRelay {

    private static final int DEFAULT_PORT = 23505;

    private MulticastSocket socket;

    private final RelayConfig relayConfig;
    private final Protocol protocol;


    public MurmurRelay(int port) {
        protocol = new Protocol();
        relayConfig = new RelayConfig("src/main/resources/relayConfig.json");

        try {
            // On crée un socket multicast
            socket = new MulticastSocket(port);
            // On rejoint le groupe de diffusion multicast
            InetAddress group = InetAddress.getByName("224.1.1.255");
            socket.joinGroup(group);

            // On écoute les messages multicast en boucle
            byte[] buffer = new byte[1024];
            while (true) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                String domain = getDomain(message);
                System.out.println("Message reçu : " + domain);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    private String getDomain(String message) throws Exception {
            //Decoupe le message en 2 parties
            Pattern pattern = Pattern.compile(protocol.getRxEcho());
            Matcher matcher = pattern.matcher(message);
            if (matcher.find()){
                String goodMessage = matcher.group(1);
                //Tester si le messaga est un domain compris dans la liste
                if (relayConfig.getDomains().contains(goodMessage)){
                    //Si oui, on decrypte le message
                    return goodMessage;
                }
        }
        return null;
    }

    public static void main(String[] args) {
        MurmurRelay murmurRelay = new MurmurRelay(DEFAULT_PORT);
    }

}

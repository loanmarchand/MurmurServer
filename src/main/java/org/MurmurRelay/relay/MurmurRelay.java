package org.MurmurRelay.relay;

import org.MurmurRelay.utils.AesUtils;
import org.MurmurRelay.utils.RelayConfig;
import org.MurmurServer.model.ApplicationData;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MurmurRelay {

    private static final int DEFAULT_PORT = 22510;

    private MulticastSocket socket;

    private AesUtils aesUtils;
    private RelayConfig relayConfig;


    public MurmurRelay(int port) {
        aesUtils = new AesUtils();
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
                String decryptedMessage = decryptMessage(message);

                System.out.println("Message reçu : " + decryptedMessage);
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

    private String decryptMessage(String message) throws Exception {
        for (String key : relayConfig.getKeys()) {
            try {
                System.out.println("Tentative de décryptage avec la clé " + key);
                return aesUtils.decrypt(message.getBytes(), aesUtils.decodeKey(key));
            } catch (Exception e) {
                // La clé ne correspond pas, on passe à la suivante
            }
        }
        // Si aucune clé ne correspond, on lève une exception
        throw new Exception("Impossible de décrypter le message, aucune clé AES valide n'a été trouvée");
    }

    public static void main(String[] args) {
        MurmurRelay murmurRelay = new MurmurRelay(DEFAULT_PORT);
    }

}

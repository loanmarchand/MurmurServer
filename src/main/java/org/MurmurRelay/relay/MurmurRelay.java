package org.MurmurRelay.relay;

import org.MurmurRelay.utils.AesUtils;
import org.MurmurServer.model.ApplicationData;
import org.MurmurServer.model.Json;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Arrays;

public class MurmurRelay {

    private static final int DEFAULT_PORT = 22510;

    private MulticastSocket socket;

    private AesUtils aesUtils;
    private ApplicationData applicationData;


    public MurmurRelay(int port) {
        aesUtils = new AesUtils();
        applicationData = new ApplicationData();
        try {
            // On crée un socket multicast
            socket = new MulticastSocket(port);

            // On rejoint le groupe de diffusion multicast
            InetAddress group = InetAddress.getByName("224.1.1.255");
            socket.joinGroup(group);

            // On écoute les messages multicast en boucle
            byte[] buffer = new byte[1024];
            while (true) {
                applicationData = Json.getApplicationData();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String message = new String(packet.getData(), 0, packet.getLength());
                //Decrypt message
                message = aesUtils.decrypt(message.getBytes(), aesUtils.decodeKey(applicationData.getBase64AES()));
                System.out.println("Message reçu : " + message);
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

    public static void main(String[] args) {
        MurmurRelay murmurRelay = new MurmurRelay(DEFAULT_PORT);
    }

}

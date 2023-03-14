package org.MurmurRelay.relay;

import org.MurmurRelay.utils.AesUtils;
import org.MurmurServer.model.Protocol;

import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServerListener implements Runnable {
    private final Socket serverSocket;
    private final MurmurRelay murmurRelay;
    private BufferedReader in;
    private Boolean isRunning;
    private AesUtils aesUtils;
    private Protocol protocol;

    public ServerListener(Socket serverSocket, MurmurRelay murmurRelay) {
        this.serverSocket = serverSocket;
        this.murmurRelay = murmurRelay;
        protocol = new Protocol();
        try {
            in = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            isRunning = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {

        try {
            String inputLine = in.readLine();
            System.out.println(inputLine);
            while (isRunning&&inputLine != null) {
                System.out.println(inputLine);
                String decrypted = aesUtils.decrypt(inputLine.getBytes(),murmurRelay.getAesKey(serverSocket));
                Pattern pattern = Pattern.compile(protocol.getRxSend());
                Matcher matcher = pattern.matcher(decrypted);
                if (matcher.find()){
                    String domain = matcher.group(2);
                    murmurRelay.sendMessage(domain,decrypted);
                }
                inputLine = in.readLine();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
}

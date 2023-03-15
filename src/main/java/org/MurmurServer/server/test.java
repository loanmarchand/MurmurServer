package org.MurmurServer.server;

import org.MurmurServer.model.Protocol;

import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
public class test {

    private static final Protocol protocol = new Protocol();

    public static void main(String[] args){
        /*String text;
        Pattern pattern = Pattern.compile(protocol.getRxSend());
        Matcher matcher = pattern.matcher("SEND 1234 server1.godswila.guru #dadzad@server2.godswila.guru FOLLOW michel@server2.godswila.guru\n");
        if (matcher.find()) {
            text = matcher.group(5);
            if(text != null){
                //J'ai un nom de dom (server2.godswila.guru)
                System.out.println(text);
            }else{
                text = matcher.group(8);
                System.out.println(text);
            }
        }*/
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        keyGenerator.init(256);
        SecretKey secretKey = keyGenerator.generateKey();
        System.out.println(Base64.getEncoder().encodeToString(secretKey.getEncoded()));
    }



}

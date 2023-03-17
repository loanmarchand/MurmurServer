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
        String text = "romain@server2.godswila.guru";
        Pattern pattern = Pattern.compile(protocol.getRxUserDomain());
        Matcher matcher = pattern.matcher(text);
        System.out.println(text);
        if (matcher.find()){
            System.out.println(true);
            for (int i = 1; i <= matcher.groupCount(); i++) {
                System.out.println(matcher.group(i) + " " + i);
            }
        }
        /*KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        keyGenerator.init(256);
        SecretKey secretKey = keyGenerator.generateKey();
        System.out.println(Base64.getEncoder().encodeToString(secretKey.getEncoded()));*/
        // Test follow avec groupe 1 et 2.
/*        String test = "FOLLOW #test@server2.godswila.guru";
        Pattern pattern = Pattern.compile(protocol.getRxFollow());
        Matcher matcher = pattern.matcher(test);
        if (matcher.find()) {
            System.out.println("Le groupe 1 : " + matcher.group(1));
            System.out.println("Le groupe 2 : " + matcher.group(2));
        }*/
    }
}

package org.MurmurServer.server;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class AesKey {

    public static void main(String[] args){
        KeyGenerator keyGenerator;
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

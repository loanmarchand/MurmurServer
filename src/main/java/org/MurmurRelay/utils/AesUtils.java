package org.MurmurRelay.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class AesUtils {

    private static final int GCM_TAG_LENGTH = 128; // in bits
    private static final int GCM_IV_LENGTH = 12; // in bytes

    public  String encrypt(String message, String secretKey) throws Exception {
        SecureRandom random = new SecureRandom();

        byte[] salt = new byte[16];
        random.nextBytes(salt);
        SecretKey key = generateKeyFromPassword(secretKey, salt);

        byte[] iv = new byte[GCM_IV_LENGTH];
        random.nextBytes(iv);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

        byte[] encryptedMessage = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
        byte[] encryptedMessageWithIVAndSalt = new byte[encryptedMessage.length + GCM_IV_LENGTH + salt.length];
        System.arraycopy(salt, 0, encryptedMessageWithIVAndSalt, 0, salt.length);
        System.arraycopy(iv, 0, encryptedMessageWithIVAndSalt, salt.length, GCM_IV_LENGTH);
        System.arraycopy(encryptedMessage, 0, encryptedMessageWithIVAndSalt, salt.length + GCM_IV_LENGTH, encryptedMessage.length);

        return Base64.getEncoder().encodeToString(encryptedMessageWithIVAndSalt);
    }

    public String decrypt(String encryptedMessageWithIVBase64, String secretKey) throws Exception {
        byte[] encryptedMessageWithIVAndSalt = Base64.getDecoder().decode(encryptedMessageWithIVBase64);
        byte[] salt = new byte[16];
        System.arraycopy(encryptedMessageWithIVAndSalt, 0, salt, 0, 16);
        SecretKey key = generateKeyFromPassword(secretKey, salt);

        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(encryptedMessageWithIVAndSalt, salt.length, iv, 0, GCM_IV_LENGTH);

        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

        byte[] encryptedMessage = new byte[encryptedMessageWithIVAndSalt.length - GCM_IV_LENGTH - salt.length];
        System.arraycopy(encryptedMessageWithIVAndSalt, salt.length + GCM_IV_LENGTH, encryptedMessage, 0, encryptedMessage.length);

        byte[] decryptedMessage = cipher.doFinal(encryptedMessage);
        return new String(decryptedMessage, StandardCharsets.UTF_8);
    }

    private SecretKey generateKeyFromPassword(String password, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256); // Vous pouvez utiliser 128, 192 ou 256
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }
}

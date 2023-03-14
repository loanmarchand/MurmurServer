package org.MurmurRelay.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static java.nio.charset.StandardCharsets.UTF_8;

public class AesUtils {

    private static final int TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;

    public  String encrypt(String plainText, SecretKey secretKey) throws Exception {
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[IV_LENGTH];
        random.nextBytes(iv);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH, iv);

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

        byte[] plainTextBytes = plainText.getBytes(StandardCharsets.UTF_8);
        byte[] cipherText = cipher.doFinal(plainTextBytes);

        byte[] cipherTextWithIv = new byte[IV_LENGTH + cipherText.length];
        System.arraycopy(iv, 0, cipherTextWithIv, 0, IV_LENGTH);
        System.arraycopy(cipherText, 0, cipherTextWithIv, IV_LENGTH, cipherText.length);

        return Base64.getEncoder().encodeToString(cipherTextWithIv);
    }

    public  String decrypt(byte[] cipherTextWithIv, SecretKey secretKey) throws Exception {
        byte[] iv = new byte[IV_LENGTH];
        System.arraycopy(cipherTextWithIv, 0, iv, 0, IV_LENGTH);

        byte[] cipherText = new byte[cipherTextWithIv.length - IV_LENGTH];
        System.arraycopy(cipherTextWithIv, IV_LENGTH, cipherText, 0, cipherText.length);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec parameterSpec = new GCMParameterSpec(TAG_LENGTH, iv);

        cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

        byte[] plainTextBytes = cipher.doFinal(cipherText);

        return new String(plainTextBytes, StandardCharsets.UTF_8);
    }

    public String encodeKey(SecretKey secretKey) {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    public SecretKey decodeKey(String base64AES) {
        return new SecretKeySpec(Base64.getDecoder().decode(base64AES), "AES");
    }
}

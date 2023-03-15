package org.MurmurRelay.utils;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.Base64;

public class AesUtils {

    private static final int GCM_TAG_LENGTH = 128; // Longueur du tag GCM en bits
    private static final int GCM_IV_LENGTH = 12; // Longueur du vecteur d'initialisation (IV) en octets

    // Méthode pour chiffrer un message avec une clé secrète
    public  String encrypt(String message, String secretKey) throws Exception {
        SecureRandom random = new SecureRandom();

        // Génération d'un sel aléatoire pour le chiffrement par mot de passe
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        SecretKey key = generateKeyFromPassword(secretKey, salt);

        // Génération d'un vecteur d'initialisation (IV) aléatoire pour le chiffrement AES-GCM
        byte[] iv = new byte[GCM_IV_LENGTH];
        random.nextBytes(iv);
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

        // Initialisation du chiffrement AES-GCM
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

        // Chiffrement du message
        byte[] encryptedMessage = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));

        // Concaténation du sel, de l'IV et du message chiffré
        byte[] encryptedMessageWithIVAndSalt = new byte[encryptedMessage.length + GCM_IV_LENGTH + salt.length];
        System.arraycopy(salt, 0, encryptedMessageWithIVAndSalt, 0, salt.length);
        System.arraycopy(iv, 0, encryptedMessageWithIVAndSalt, salt.length, GCM_IV_LENGTH);
        System.arraycopy(encryptedMessage, 0, encryptedMessageWithIVAndSalt, salt.length + GCM_IV_LENGTH, encryptedMessage.length);

        // Encodage en Base64 du message chiffré avec sel et IV
        return Base64.getEncoder().encodeToString(encryptedMessageWithIVAndSalt);
    }

    // Méthode pour déchiffrer un message avec une clé secrète
    public String decrypt(String encryptedMessageWithIVBase64, String secretKey) throws Exception {
        // Décodage du message chiffré en Base64
        byte[] encryptedMessageWithIVAndSalt = Base64.getDecoder().decode(encryptedMessageWithIVBase64);

        // Extraction du sel du message chiffré
        byte[] salt = new byte[16];
        System.arraycopy(encryptedMessageWithIVAndSalt, 0, salt, 0, 16);
        SecretKey key = generateKeyFromPassword(secretKey, salt);

        // Extraction de l'IV du message chiffré
        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(encryptedMessageWithIVAndSalt, salt.length, iv, 0, GCM_IV_LENGTH);

        // Initialisation du déchiffrement AES-GCM
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

        // Extraction du message chiffré
        byte[] encryptedMessage = new byte[encryptedMessageWithIVAndSalt.length - GCM_IV_LENGTH - salt.length];
        System.arraycopy(encryptedMessageWithIVAndSalt, salt.length + GCM_IV_LENGTH, encryptedMessage, 0, encryptedMessage.length);

        // Déchiffrement du message
        byte[] decryptedMessage = cipher.doFinal(encryptedMessage);
        return new String(decryptedMessage, StandardCharsets.UTF_8);
    }

    // Méthode pour générer une clé secrète à partir d'un mot de passe et d'un sel
    private SecretKey generateKeyFromPassword(String password, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 256); // Vous pouvez utiliser 128, 192 ou 256
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), "AES");
    }
}

package com.pilming.iot_sensor.util;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAUtils {
    public static String encrypt(String data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }


    public static String decrypt(String encryptedData, String privateKeyStr) throws Exception {
        PrivateKey privateKey = getPrivateKeyFromString(privateKeyStr);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedBytes);
    }

    public static PublicKey getPublicKeyFromString(String publicKeyPEM) throws Exception {
        // PEM 헤더와 개행 제거
        String publicKeyContent = publicKeyPEM
                .replaceAll("-----BEGIN PUBLIC KEY-----", "")
                .replaceAll("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "") // ✅ 모든 공백 및 개행문자 제거
                .trim();

        // Base64 디코딩 후 키 객체로 변환
        byte[] decodedKey = Base64.getDecoder().decode(publicKeyContent);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        return keyFactory.generatePublic(keySpec);
    }


    private static PrivateKey getPrivateKeyFromString(String key) throws Exception {
        byte[] byteKey = Base64.getDecoder().decode(key
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s", ""));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(byteKey));
    }
}

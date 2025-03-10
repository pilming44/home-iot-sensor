package com.pilming.iot_sensor.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AESUtils {

    private static final String AES_ALGORITHM = "AES";

    public static String encrypt(String data, byte[] aesKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKeySpec secretKeySpec = new SecretKeySpec(aesKey, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    public static String decrypt(String encryptedData, String secretKey) throws Exception {
        SecretKey key = new SecretKeySpec(secretKey.getBytes(), AES_ALGORITHM);
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedBytes);
    }

    public static String generateAESKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM);
        keyGenerator.init(256);
        SecretKey secretKey = keyGenerator.generateKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }
}

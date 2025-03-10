package com.pilming.iot_sensor.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class HMACUtils {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    public static String generateHMAC(String data, String secretKey) throws Exception {
        Mac mac = Mac.getInstance(HMAC_ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(), HMAC_ALGORITHM);
        mac.init(secretKeySpec);
        byte[] hmacBytes = mac.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(hmacBytes);
    }
}

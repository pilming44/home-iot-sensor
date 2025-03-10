package com.pilming.iot_sensor.util;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class RSAKeyConfig {

    @Value("${security.rsa.public-key}")
    private String publicKey;

    @Value("${security.rsa.private-key}")
    private String privateKey;

}

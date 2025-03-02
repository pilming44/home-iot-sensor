package com.pilming.iot_sensor.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * 센서 유형을 정의하는 Enum
 */
@Getter
public enum SensorType {
    TEMPERATURE_HUMIDITY("DHT22", List.of(SensorDataKey.TEMPERATURE, SensorDataKey.HUMIDITY));

    private final String code; // 센서 코드
    private final List<SensorDataKey> supportedKeys; // 센서가 제공하는 데이터 키

    SensorType(String code, List<SensorDataKey> supportedKeys) {
        this.code = code;
        this.supportedKeys = supportedKeys;
    }

    public static SensorType fromCode(String code) {
        return Arrays.stream(values())
                .filter(type -> type.code.equalsIgnoreCase(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid SensorType: " + code));
    }
}

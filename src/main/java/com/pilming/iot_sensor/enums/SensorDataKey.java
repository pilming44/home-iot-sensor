package com.pilming.iot_sensor.enums;

import lombok.Getter;

/**
 * 센서 데이터의 키를 정의하는 Enum
 */
@Getter
public enum SensorDataKey {
    TEMPERATURE("temperature"),
    HUMIDITY("humidity");

    private final String key; // DB 저장용 키 값

    SensorDataKey(String key) {
        this.key = key;
    }

    public static SensorDataKey fromKey(String key) {
        for (SensorDataKey dataKey : values()) {
            if (dataKey.key.equalsIgnoreCase(key)) {
                return dataKey;
            }
        }
        throw new IllegalArgumentException("Invalid SensorDataKey: " + key);
    }
}

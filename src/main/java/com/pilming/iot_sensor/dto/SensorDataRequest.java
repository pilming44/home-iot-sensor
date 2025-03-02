package com.pilming.iot_sensor.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SensorDataRequest {
    private String key;
    private String value;
}

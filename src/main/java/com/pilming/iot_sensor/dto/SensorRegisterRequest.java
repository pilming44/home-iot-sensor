package com.pilming.iot_sensor.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SensorRegisterRequest {
    private String sensorUid;
    private String sensorType;
    private String name;
}

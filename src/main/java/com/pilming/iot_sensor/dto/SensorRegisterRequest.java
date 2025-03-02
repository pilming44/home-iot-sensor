package com.pilming.iot_sensor.dto;

import com.pilming.iot_sensor.enums.SensorType;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SensorRegisterRequest {
    @NotNull
    private String sensorUid;

    @NotNull
    private SensorType sensorType;

    private String name;
}

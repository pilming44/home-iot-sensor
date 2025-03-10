package com.pilming.iot_sensor.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SensorDataValueDto {
    private String dataKey;
    private String dataValue;
}

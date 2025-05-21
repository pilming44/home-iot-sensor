package com.pilming.iot_sensor.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SensorStatusResponseDto {
    private String sensorUid;
    private String sensorName;
    private String sensorStatus;
    private LocalDateTime lastUpdate;
}

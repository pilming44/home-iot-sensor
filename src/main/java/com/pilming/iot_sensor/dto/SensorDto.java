package com.pilming.iot_sensor.dto;

import com.pilming.iot_sensor.entity.Sensor;
import com.pilming.iot_sensor.enums.SensorType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class SensorDto {
    private Long id;
    private String sensorUid;
    private SensorType sensorType;
    private String name;
    private int transmissionInterval;
    private LocalDateTime lastTransmissionTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static SensorDto toDto(Sensor sensor) {
        return new SensorDto(
                sensor.getId(),
                sensor.getSensorUid(),
                sensor.getSensorType(),
                sensor.getName(),
                sensor.getTransmissionInterval(),
                sensor.getLastTransmissionTime(),
                sensor.getCreatedAt(),
                sensor.getUpdatedAt()
        );
    }
}

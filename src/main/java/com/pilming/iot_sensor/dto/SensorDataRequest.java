package com.pilming.iot_sensor.dto;

import com.pilming.iot_sensor.enums.SensorDataKey;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SensorDataRequest {
    @NotNull
    private SensorDataKey dataKey;

    @NotNull
    private String dataValue;
}

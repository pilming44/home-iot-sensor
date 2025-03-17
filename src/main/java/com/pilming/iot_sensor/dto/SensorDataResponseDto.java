package com.pilming.iot_sensor.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class SensorDataResponseDto {
    private List<String> timestamps;
    private List<DatasetDto> datasets;

    @Builder
    @Getter
    public static class DatasetDto {
        private String label;
        private List<Double> data;
        private String borderColor;
    }
}

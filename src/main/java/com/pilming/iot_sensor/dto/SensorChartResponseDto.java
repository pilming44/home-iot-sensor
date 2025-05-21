package com.pilming.iot_sensor.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Getter
@Builder
@Jacksonized
@AllArgsConstructor
public class SensorChartResponseDto {
    private List<DatasetDto> datasets;

    @Getter
    @Builder
    @Jacksonized
    public static class DatasetDto {
        private String label;
        private List<String> timestamps;
        private List<Double> data;
    }
}

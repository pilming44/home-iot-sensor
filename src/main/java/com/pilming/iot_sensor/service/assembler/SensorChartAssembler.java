package com.pilming.iot_sensor.service.assembler;

import com.pilming.iot_sensor.dto.SensorChartResponseDto;
import com.pilming.iot_sensor.entity.Sensor;
import com.pilming.iot_sensor.entity.SensorData;
import com.pilming.iot_sensor.enums.SensorDataKey;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Component
public class SensorChartAssembler {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("MM-dd HH:mm:ss");

    public SensorChartResponseDto toDto(
            List<Sensor> sensors, List<SensorData> allData) {

        // 1) 센서->데이터 매핑
        Map<Sensor, List<SensorData>> bySensor = allData.stream()
                .collect(groupingBy(
                        SensorData::getSensor,
                        LinkedHashMap::new,
                        toList()
                ));

        // 2) 센서×타입별 DatasetDto 생성
        List<SensorChartResponseDto.DatasetDto> datasets = new ArrayList<>();
        for (Sensor sensor : sensors) {
            List<SensorData> list = bySensor.getOrDefault(sensor, List.of());
            datasets.addAll(buildSensorDatasets(sensor, list));
        }

        return SensorChartResponseDto.builder()
                .datasets(datasets)
                .build();
    }

    private List<SensorChartResponseDto.DatasetDto> buildSensorDatasets(
            Sensor sensor, List<SensorData> data) {

        // 데이터 타입별로 그룹핑
        Map<SensorDataKey, List<SensorData>> byKey = data.stream()
                .collect(groupingBy(
                        SensorData::getDataKey,
                        LinkedHashMap::new,
                        toList()
                ));

        String name = sensor.getName() != null && !sensor.getName().isBlank()
                ? sensor.getName()
                : sensor.getSensorUid();

        // 각 타입별로 DTO
        return byKey.entrySet().stream()
                .map(e -> makeDataset(name, e.getKey(), e.getValue()))
                .toList();
    }

    private SensorChartResponseDto.DatasetDto makeDataset(
            String sensorName,
            SensorDataKey key,
            List<SensorData> list
    ) {
        // 1) 시간순 정렬
        list.sort(comparing(SensorData::getTimestamp));

        // 2) 포맷 후 리스트 추출
        List<String> ts = list.stream()
                .map(sd -> sd.getTimestamp()
                        .truncatedTo(ChronoUnit.SECONDS)
                        .format(FMT))
                .toList();
        List<Double> vals = list.stream()
                .map(sd -> Double.valueOf(sd.getDataValue()))
                .toList();

        // 3) 라벨
        String label = sensorName + " " + key.name();

        return SensorChartResponseDto.DatasetDto.builder()
                .label(label)
                .timestamps(ts)
                .data(vals)
                .build();
    }
}

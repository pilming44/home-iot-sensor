package com.pilming.iot_sensor.controller;

import com.pilming.iot_sensor.dto.SensorDataRequest;
import com.pilming.iot_sensor.dto.SensorDataResponseDto;
import com.pilming.iot_sensor.dto.SensorDataValueDto;
import com.pilming.iot_sensor.dto.SensorRegisterRequest;
import com.pilming.iot_sensor.entity.Sensor;
import com.pilming.iot_sensor.entity.SensorData;
import com.pilming.iot_sensor.exception.SensorNotFoundException;
import com.pilming.iot_sensor.service.SensorService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/sensors")
@RequiredArgsConstructor
public class SensorController {

    private final SensorService sensorService;

    @PostMapping("/register")
    public ResponseEntity<Sensor> registerSensor(@RequestBody SensorRegisterRequest request) {
        return ResponseEntity.ok(sensorService.registerSensor(request));
    }

    @GetMapping
    public ResponseEntity<List<Sensor>> getAllSensors() {
        return ResponseEntity.ok(sensorService.getAllSensors());
    }

    @PostMapping("/{sensorUid}/data")
    public ResponseEntity<SensorData> saveSensorData(
            @PathVariable("sensorUid") String sensorUid, @RequestBody SensorDataRequest request) {
        return ResponseEntity.ok(sensorService.saveSensorData(sensorUid, request));
    }

    @GetMapping("/{sensorUid}/data")
    public ResponseEntity<List<SensorDataValueDto>> getSensorData(@PathVariable("sensorUid") String sensorUid) {
        List<SensorDataValueDto> dataList = sensorService.getSensorData(sensorUid);
        return ResponseEntity.ok(dataList);
    }

    @GetMapping("/chart-data")
    public ResponseEntity<SensorDataResponseDto> getSensorChartData(
            @RequestParam(name = "sensorUid", required = false) String sensorUid,
            @RequestParam(name = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(name = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        try {
            SensorDataResponseDto response = sensorService.getSensorChartData(sensorUid, from, to);
            if (response.getDatasets().isEmpty()) {
                return ResponseEntity.ok(getEmptySensorDataResponse());
            }
            return ResponseEntity.ok(response);
        } catch (SensorNotFoundException e) {
            // 빈 차트용 데이터 반환
            return ResponseEntity.ok(getEmptySensorDataResponse());
        }
    }

    private SensorDataResponseDto getEmptySensorDataResponse() {
        return SensorDataResponseDto.builder()
                .timestamps(Collections.emptyList())
                .datasets(Collections.emptyList())
                .build();
    }
}

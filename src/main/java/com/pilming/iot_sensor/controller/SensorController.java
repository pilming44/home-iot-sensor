package com.pilming.iot_sensor.controller;

import com.pilming.iot_sensor.dto.*;
import com.pilming.iot_sensor.entity.Sensor;
import com.pilming.iot_sensor.entity.SensorData;
import com.pilming.iot_sensor.exception.SensorNotFoundException;
import com.pilming.iot_sensor.service.SensorService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/sensors")
@RequiredArgsConstructor
public class SensorController {

    private final SensorService sensorService;

    @PostMapping("/register")
    public ResponseEntity<Sensor> registerSensor(@RequestBody SensorRegisterRequest request) {
        sensorService.registerSensor(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<SensorDto>> getAllSensors() {
        return ResponseEntity.ok(sensorService.getAllSensors());
    }

    @PostMapping("/{sensorUid}/data")
    public ResponseEntity<SensorData> saveSensorData(
            @PathVariable("sensorUid") String sensorUid, @RequestBody SensorDataRequest request) {
        sensorService.saveSensorData(sensorUid, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{sensorUid}/data")
    public ResponseEntity<List<SensorDataValueDto>> getSensorData(@PathVariable("sensorUid") String sensorUid) {
        List<SensorDataValueDto> dataList = sensorService.getSensorData(sensorUid);
        return ResponseEntity.ok(dataList);
    }

    @GetMapping("/chart-data")
    public ResponseEntity<SensorChartResponseDto> getSensorChartData(
            @RequestParam(name = "sensorUid", required = false) String sensorUid,
            @RequestParam(name = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(name = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {

        try {
            SensorChartResponseDto response = sensorService.getSensorChartData(sensorUid, from, to);
            if (response.getDatasets().isEmpty()) {
                return ResponseEntity.ok(new SensorChartResponseDto(Collections.emptyList()));
            }
            return ResponseEntity.ok(response);
        } catch (SensorNotFoundException e) {
            // 빈 차트용 데이터 반환
            return ResponseEntity.ok(new SensorChartResponseDto(Collections.emptyList()));
        }
    }

    @GetMapping("/{sensorUid}/status")
    public ResponseEntity<SensorStatusResponseDto> getStatus(@PathVariable("sensorUid") String sensorUid) {
        SensorStatusResponseDto dto = sensorService.getSensorStatus(sensorUid);
        return ResponseEntity.ok(dto);
    }
}

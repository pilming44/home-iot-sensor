package com.pilming.iot_sensor.controller;

import com.pilming.iot_sensor.dto.SensorDataRequest;
import com.pilming.iot_sensor.dto.SensorRegisterRequest;
import com.pilming.iot_sensor.entity.Sensor;
import com.pilming.iot_sensor.entity.SensorData;
import com.pilming.iot_sensor.service.SensorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sensors")
@RequiredArgsConstructor
public class SensorController {

    private final SensorService sensorService;

    @PostMapping
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
    public ResponseEntity<List<SensorData>> getSensorData(@PathVariable("sensorUid") String sensorUid) {
        return ResponseEntity.ok(sensorService.getSensorData(sensorUid));
    }
}

package com.pilming.iot_sensor.service;

import com.pilming.iot_sensor.dto.SensorDataRequest;
import com.pilming.iot_sensor.dto.SensorRegisterRequest;
import com.pilming.iot_sensor.entity.Sensor;
import com.pilming.iot_sensor.entity.SensorData;
import com.pilming.iot_sensor.entity.SensorDataValue;
import com.pilming.iot_sensor.repository.SensorDataRepository;
import com.pilming.iot_sensor.repository.SensorDataValueRepository;
import com.pilming.iot_sensor.repository.SensorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SensorService {

    private final SensorRepository sensorRepository;
    private final SensorDataRepository sensorDataRepository;
    private final SensorDataValueRepository sensorDataValueRepository;

    public Sensor registerSensor(SensorRegisterRequest request) {
        Sensor sensor = Sensor.builder()
                .sensorUid(request.getSensorUid())
                .sensorType(request.getSensorType())
                .name(request.getName())
                .createdAt(LocalDateTime.now())
                .build();
        return sensorRepository.save(sensor);
    }

    public List<Sensor> getAllSensors() {
        return sensorRepository.findAll();
    }

    public SensorData saveSensorData(String sensorUid, SensorDataRequest request) {
        Sensor sensor = sensorRepository.findBySensorUid(sensorUid)
                .orElseThrow(() -> new RuntimeException("Sensor not found"));

        SensorData sensorData = SensorData.builder()
                .sensor(sensor)
                .timestamp(LocalDateTime.now())
                .build();
        sensorDataRepository.save(sensorData);

        SensorDataValue dataValue = SensorDataValue.builder()
                .sensorData(sensorData)
                .dataKey(request.getDataKey())
                .dataValue(request.getDataValue())
                .build();
        sensorDataValueRepository.save(dataValue);

        return sensorData;
    }

    public List<SensorData> getSensorData(String sensorUid) {
        Sensor sensor = sensorRepository.findBySensorUid(sensorUid)
                .orElseThrow(() -> new RuntimeException("Sensor not found"));
        return sensorDataRepository.findBySensor(sensor);
    }
}

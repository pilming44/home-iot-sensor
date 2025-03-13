package com.pilming.iot_sensor.service;

import com.pilming.iot_sensor.dto.SensorDataRequest;
import com.pilming.iot_sensor.dto.SensorDataValueDto;
import com.pilming.iot_sensor.dto.SensorRegisterRequest;
import com.pilming.iot_sensor.entity.Sensor;
import com.pilming.iot_sensor.entity.SensorData;
import com.pilming.iot_sensor.entity.SensorDataValue;
import com.pilming.iot_sensor.entity.SensorStatus;
import com.pilming.iot_sensor.exception.DuplicateSensorException;
import com.pilming.iot_sensor.exception.SensorNotFoundException;
import com.pilming.iot_sensor.repository.SensorDataRepository;
import com.pilming.iot_sensor.repository.SensorDataValueRepository;
import com.pilming.iot_sensor.repository.SensorRepository;
import com.pilming.iot_sensor.repository.SensorStatusRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SensorService {

    private final SensorRepository sensorRepository;
    private final SensorDataRepository sensorDataRepository;
    private final SensorDataValueRepository sensorDataValueRepository;
    private final SensorStatusRepository sensorStatusRepository;

    public Sensor registerSensor(SensorRegisterRequest request) {
        // 이미 존재하는 센서인지 확인
        if (sensorRepository.existsBySensorUid(request.getSensorUid())) {
            throw new DuplicateSensorException("이미 등록된 센서입니다: " + request.getSensorUid());
        }

        LocalDateTime now = LocalDateTime.now();

        Sensor sensor = Sensor.builder()
                .sensorUid(request.getSensorUid())
                .sensorType(request.getSensorType())
                .name(request.getName())
                .transmissionInterval(request.getTransmissionInterval())
                .createdAt(now)
                .build();
        sensorRepository.save(sensor);

        SensorStatus sensorStatus = SensorStatus.builder()
                .sensor(sensor)
                .sensorStatus("OFFLINE")
                .lastUpdate(now)
                .build();
        sensorStatusRepository.save(sensorStatus);

        return sensor;
    }

    public List<Sensor> getAllSensors() {
        return sensorRepository.findAll();
    }

    public SensorData saveSensorData(String sensorUid, SensorDataRequest request) {
        Sensor sensor = sensorRepository.findBySensorUid(sensorUid)
                .orElseThrow(() -> new SensorNotFoundException("해당 UID를 가진 센서를 찾을 수 없습니다: " + sensorUid));

        LocalDateTime now = LocalDateTime.now();

        SensorData sensorData = SensorData.builder()
                .sensor(sensor)
                .timestamp(now)
                .build();

        SensorData savedData = sensorDataRepository.save(sensorData);

        SensorDataValue sensorDataValue = SensorDataValue.builder()
                .sensorData(savedData)
                .dataKey(request.getDataKey())
                .dataValue(request.getDataValue())
                .build();

        sensorDataValueRepository.save(sensorDataValue);

        //마지막 데이터 전송 시간 기록 -> 센서 상태체크용
        sensor.markAsDataTransmitted(now);

        return savedData;
    }

    public List<SensorDataValueDto> getSensorData(String sensorUid) {
        Sensor sensor = sensorRepository.findBySensorUid(sensorUid)
                .orElseThrow(() -> new SensorNotFoundException("센서를 찾을 수 없습니다: " + sensorUid));

        List<SensorDataValue> sensorDataValues = sensorDataValueRepository.findBySensorDataSensor(sensor);

        return sensorDataValues.stream()
                .map(data -> new SensorDataValueDto(data.getDataKey().name(), data.getDataValue()))
                .collect(Collectors.toList());
    }
}

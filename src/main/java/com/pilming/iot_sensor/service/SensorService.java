package com.pilming.iot_sensor.service;

import com.pilming.iot_sensor.dto.*;
import com.pilming.iot_sensor.entity.Sensor;
import com.pilming.iot_sensor.entity.SensorData;
import com.pilming.iot_sensor.entity.SensorStatus;
import com.pilming.iot_sensor.exception.DuplicateSensorException;
import com.pilming.iot_sensor.exception.SensorNotFoundException;
import com.pilming.iot_sensor.repository.SensorDataRepository;
import com.pilming.iot_sensor.repository.SensorRepository;
import com.pilming.iot_sensor.repository.SensorStatusRepository;
import com.pilming.iot_sensor.service.assembler.SensorChartAssembler;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SensorService {

    private final SensorRepository sensorRepository;
    private final SensorDataRepository sensorDataRepository;
    private final SensorStatusRepository sensorStatusRepository;
    private final SensorChartAssembler assembler;

    public void registerSensor(SensorRegisterRequest request) {
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
    }

    public List<SensorDto> getAllSensors() {
        List<Sensor> allSensor = sensorRepository.findAll();
        return allSensor.stream()
                .map(SensorDto::toDto)
                .collect(Collectors.toList());
    }

    public void saveSensorData(String sensorUid, SensorDataRequest request) {
        Sensor sensor = sensorRepository.findBySensorUid(sensorUid)
                .orElseThrow(() -> new SensorNotFoundException("해당 UID를 가진 센서를 찾을 수 없습니다: " + sensorUid));

        LocalDateTime now = LocalDateTime.now();

        SensorData sensorData = SensorData.builder()
                .sensor(sensor)
                .dataKey(request.getDataKey())
                .dataValue(request.getDataValue())
                .timestamp(now)
                .build();

        SensorData savedData = sensorDataRepository.save(sensorData);

        //마지막 데이터 전송 시간 기록 -> 센서 상태체크용
        sensor.markAsDataTransmitted(now);
    }

    public List<SensorDataValueDto> getSensorData(String sensorUid) {
        Sensor sensor = sensorRepository.findBySensorUid(sensorUid)
                .orElseThrow(() -> new SensorNotFoundException("센서를 찾을 수 없습니다: " + sensorUid));

        List<SensorData> sensorData = sensorDataRepository.findSensorData(sensor, null, null);

        return sensorData.stream()
                .map(data -> new SensorDataValueDto(
                                data.getDataKey().name(),
                                data.getDataValue(),
                                data.getTimestamp().format(DateTimeFormatter.ofPattern("MM-dd HH:mm"))
                        )
                )
                .collect(Collectors.toList());
    }

    public SensorChartResponseDto getSensorChartData(String sensorUid, LocalDate from, LocalDate to) {
        // 센서 조회
        List<Sensor> sensors = loadSensors(sensorUid);

        // 기간 파라미터 변환
        LocalDateTime fromDt = toDateTime(from, true);
        LocalDateTime toDt = toDateTime(to, false);

        // 센서별 데이터 조회
        List<SensorData> allData = sensors.stream()
                .flatMap(s -> sensorDataRepository.findSensorData(s, fromDt, toDt).stream())
                .toList();

        // 어셈블러에 위임
        return assembler.toDto(sensors, allData);
    }

    private List<Sensor> loadSensors(String sensorUid) {
        if (sensorUid == null || sensorUid.isBlank()) {
            return sensorRepository.findAll();
        }
        Sensor s = sensorRepository.findBySensorUid(sensorUid)
                .orElseThrow(() -> new SensorNotFoundException(sensorUid));
        return List.of(s);
    }

    private LocalDateTime toDateTime(LocalDate d, boolean startOfDay) {
        if (d == null) return null;
        return startOfDay
                ? d.atStartOfDay()
                : d.plusDays(1).atStartOfDay();
    }

}

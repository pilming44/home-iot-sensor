package com.pilming.iot_sensor.monitoring;

import com.pilming.iot_sensor.entity.Sensor;
import com.pilming.iot_sensor.entity.SensorStatus;
import com.pilming.iot_sensor.repository.SensorRepository;
import com.pilming.iot_sensor.repository.SensorStatusRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class SensorStatusScheduler {

    private final SensorRepository sensorRepository;
    private final SensorStatusRepository sensorStatusRepository;

    @PostConstruct
    public void init() {
        log.info("SensorStatusScheduler 초기화 완료");
    }

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void updateSensorStatus() {
        log.debug("updateSensorStatus 스케쥴 실행");
        List<Sensor> sensors = sensorRepository.findAll();
        for (Sensor sensor : sensors) {
            SensorStatus existingStatus = sensorStatusRepository.findBySensor(sensor)
                    .orElse(null);

            String newStatus = determineStatus(sensor);

            if (existingStatus == null) {
                SensorStatus updatedStatus = SensorStatus.builder()
                        .sensor(sensor)
                        .sensorStatus(newStatus)
                        .lastUpdate(LocalDateTime.now())
                        .build();
                sensorStatusRepository.save(updatedStatus);
                log.debug("상태 체크 등록. 센서 : {}", updatedStatus.getSensor().getSensorUid());
            } else if (!existingStatus.getSensorStatus().equals(newStatus)) {
                existingStatus.updateStatus(newStatus, LocalDateTime.now());
                log.debug("상태 변경됨. 센서 : {}, 상태: {}", existingStatus.getSensor().getSensorUid(), existingStatus.getSensorStatus());
            }
        }
    }

    private String determineStatus(Sensor sensor) {
        if (sensor.getLastTransmissionTime() == null ||
                sensor.getLastTransmissionTime().isBefore(LocalDateTime.now().minusSeconds(sensor.getTransmissionInterval() * 2))) {
            return "OFFLINE";
        }
        return "ONLINE";
    }
}


package com.pilming.iot_sensor.monitoring;

import com.pilming.iot_sensor.entity.Sensor;
import com.pilming.iot_sensor.entity.SensorStatus;
import com.pilming.iot_sensor.repository.SensorRepository;
import com.pilming.iot_sensor.repository.SensorStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SensorStatusScheduler {

    private final SensorRepository sensorRepository;
    private final SensorStatusRepository sensorStatusRepository;

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void updateSensorStatus() {
        List<Sensor> sensors = sensorRepository.findAll();
        for (Sensor sensor : sensors) {
            SensorStatus existingStatus = sensorStatusRepository.findBySensor(sensor)
                    .orElse(null);

            String newStatus = determineStatus(sensor);

            SensorStatus updatedStatus;
            if (existingStatus == null) {
                updatedStatus = SensorStatus.builder()
                        .sensor(sensor)
                        .sensorStatus(newStatus)
                        .lastUpdate(LocalDateTime.now())
                        .build();
            } else {
                updatedStatus = existingStatus.updateStatus(newStatus);
            }

            sensorStatusRepository.save(updatedStatus);
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


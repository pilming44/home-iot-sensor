package com.pilming.iot_sensor.repository;

import com.pilming.iot_sensor.entity.Sensor;
import com.pilming.iot_sensor.entity.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SensorDataRepository extends JpaRepository<SensorData, Long> {
    List<SensorData> findBySensorAndTimestampBetween(Sensor sensor, LocalDateTime from, LocalDateTime to);
}

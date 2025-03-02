package com.pilming.iot_sensor.repository;

import com.pilming.iot_sensor.entity.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SensorRepository extends JpaRepository<Sensor, Long> {
    Optional<Sensor> findBySensorUid(String sensorUid);
}

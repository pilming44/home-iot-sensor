package com.pilming.iot_sensor.repository;

import com.pilming.iot_sensor.entity.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorDataRepository extends JpaRepository<SensorData, Long> {
}

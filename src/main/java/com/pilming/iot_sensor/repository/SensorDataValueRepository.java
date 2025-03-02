package com.pilming.iot_sensor.repository;

import com.pilming.iot_sensor.entity.SensorDataValue;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorDataValueRepository extends JpaRepository<SensorDataValue, Long> {
}

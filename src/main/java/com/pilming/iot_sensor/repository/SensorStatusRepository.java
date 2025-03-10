package com.pilming.iot_sensor.repository;

import com.pilming.iot_sensor.entity.Sensor;
import com.pilming.iot_sensor.entity.SensorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SensorStatusRepository extends JpaRepository<SensorStatus, Long> {
    Optional<SensorStatus> findBySensor(Sensor sensor);
}

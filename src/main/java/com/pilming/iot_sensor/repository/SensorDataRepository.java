package com.pilming.iot_sensor.repository;

import com.pilming.iot_sensor.entity.Sensor;
import com.pilming.iot_sensor.entity.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SensorDataRepository extends JpaRepository<SensorData, Long> {
    List<SensorData> findBySensorAndTimestampBetween(Sensor sensor, LocalDateTime from, LocalDateTime to);

    @Query("SELECT s FROM SensorData s " +
            "WHERE (:sensor is null or s.sensor = :sensor) " +
            "AND (:from is null or s.timestamp >= :from) " +
            "AND (:to is null or s.timestamp < :to)")
    List<SensorData> findSensorData(@Param("sensor") Sensor sensor,
                                    @Param("from") LocalDateTime from,
                                    @Param("to") LocalDateTime to);

}

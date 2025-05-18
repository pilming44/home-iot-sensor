package com.pilming.iot_sensor.repository;

import com.pilming.iot_sensor.entity.Sensor;
import com.pilming.iot_sensor.entity.SensorData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SensorDataRepository extends JpaRepository<SensorData, Long> {
    @Query("SELECT sd FROM SensorData sd " +
            "WHERE (:sensor is null or sd.sensor = :sensor) " +
            "AND (:from is null or sd.timestamp >= :from) " +
            "AND (:to is null or sd.timestamp < :to) " +
            "ORDER BY sd.timestamp ASC"
    )
    List<SensorData> findSensorData(@Param("sensor") Sensor sensor,
                                    @Param("from") LocalDateTime from,
                                    @Param("to") LocalDateTime to);
}

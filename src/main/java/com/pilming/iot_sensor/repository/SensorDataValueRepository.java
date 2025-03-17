package com.pilming.iot_sensor.repository;

import com.pilming.iot_sensor.entity.Sensor;
import com.pilming.iot_sensor.entity.SensorData;
import com.pilming.iot_sensor.entity.SensorDataValue;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SensorDataValueRepository extends JpaRepository<SensorDataValue, Long> {
    List<SensorDataValue> findBySensorDataSensor(Sensor sensor);

    List<SensorDataValue> findBySensorData(SensorData sensorData);
}

package com.pilming.iot_sensor.entity;

import com.pilming.iot_sensor.enums.SensorDataKey;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sensor_data_values")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorDataValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_data_id", nullable = false)
    private SensorData sensorData;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SensorDataKey dataKey;  //센서데이터 키

    @Column(nullable = false)
    private String dataValue;  // 센서데이터 값
}

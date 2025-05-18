package com.pilming.iot_sensor.entity;

import com.pilming.iot_sensor.enums.SensorDataKey;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_data")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SensorDataKey dataKey;  //센서데이터 키

    @Column(nullable = false)
    private String dataValue;  // 센서데이터 값

    @Column(nullable = false)
    private LocalDateTime timestamp;  // 데이터 수집 시간
}


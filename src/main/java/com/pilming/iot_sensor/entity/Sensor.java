package com.pilming.iot_sensor.entity;

import com.pilming.iot_sensor.enums.SensorType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "sensors")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sensor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String sensorUid;  // 센서의 고유 ID (UUID)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SensorType sensorType;  //센서 종류

    @Column
    private String name;

    @Column(nullable = false)
    private int transmissionInterval; // 데이터 전송 주기 (초 단위)

    @Column
    private LocalDateTime lastTransmissionTime; // 마지막 데이터 전송 시간

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;
}

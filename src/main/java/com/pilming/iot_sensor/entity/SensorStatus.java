package com.pilming.iot_sensor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "sensors_status")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    @Column(nullable = false)
    private String sensorStatus; // ONLINE, OFFLINE

    @Column(nullable = false)
    private LocalDateTime lastUpdate;

    public void updateStatus(String newStatus, LocalDateTime lastUpdate) {
        this.sensorStatus = newStatus;
        this.lastUpdate = lastUpdate;
    }
}

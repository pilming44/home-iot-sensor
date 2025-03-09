package com.pilming.iot_sensor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sensors_keys")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SensorKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "sensor_id", nullable = false)
    private Sensor sensor;

    @Column(nullable = false)
    private String securityKey; // AES 대칭키

    public void updateKey(String newKey) {
        this.securityKey = newKey;
    }
}

package com.pilming.iot_sensor.service;

import com.pilming.iot_sensor.entity.SensorStatus;
import com.pilming.iot_sensor.repository.SensorStatusRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SensorStatusService {

    private final SensorStatusRepository sensorStatusRepository;

    public List<SensorStatus> getAllStatuses() {
        return sensorStatusRepository.findAll();
    }
}

package com.pilming.iot_sensor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class IotSensorApplication {

    public static void main(String[] args) {
        SpringApplication.run(IotSensorApplication.class, args);
    }

}

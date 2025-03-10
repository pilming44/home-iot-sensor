package com.pilming.iot_sensor.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) // 409 상태 코드 반환
public class DuplicateSensorException extends RuntimeException {
    public DuplicateSensorException(String message) {
        super(message);
    }
}

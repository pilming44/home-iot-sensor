package com.pilming.iot_sensor.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleAllExceptions(Exception ex) {
        log.error("예외 발생: {}", ex.getMessage(), ex);  // 전체 예외 로그 출력
        return new ResponseEntity<>("서버 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(SensorNotFoundException.class)
    public ResponseEntity<String> handleSensorNotFound(SensorNotFoundException ex) {
        log.warn("센서를 찾을 수 없음: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateSensorException.class)
    public ResponseEntity<String> handleDuplicateSensor(DuplicateSensorException ex) {
        log.warn("중복 센서 등록 시도: {}", ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT);
    }
}

package com.pilming.iot_sensor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pilming.iot_sensor.dto.SensorDataRequest;
import com.pilming.iot_sensor.dto.SensorRegisterRequest;
import com.pilming.iot_sensor.entity.Sensor;
import com.pilming.iot_sensor.entity.SensorStatus;
import com.pilming.iot_sensor.enums.SensorDataKey;
import com.pilming.iot_sensor.enums.SensorType;
import com.pilming.iot_sensor.exception.SensorNotFoundException;
import com.pilming.iot_sensor.monitoring.SensorStatusScheduler;
import com.pilming.iot_sensor.repository.SensorRepository;
import com.pilming.iot_sensor.repository.SensorStatusRepository;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class SensorControllerTest {

    private static String SENSOR_UUID = "sensor-123";
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SensorStatusRepository sensorStatusRepository;
    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private SensorStatusScheduler sensorStatusScheduler;

    @BeforeEach
    void setUp() throws Exception {
//        assertNotNull(rsaKeyConfig.getPublicKey(), "RSA 공개키가 로드되지 않았습니다.");

        SensorRegisterRequest sensorRequest = new SensorRegisterRequest();
        sensorRequest.setSensorUid(SENSOR_UUID);
        sensorRequest.setSensorType(SensorType.TEMPERATURE_HUMIDITY);
        sensorRequest.setName("TempSensor1");
        sensorRequest.setTransmissionInterval(60);
        ResultActions result = mockMvc.perform(post("/api/sensors/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sensorRequest)))
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("센서 정상 등록")
    void registerSensor() throws Exception {
        SensorRegisterRequest request = new SensorRegisterRequest();
        request.setSensorUid("sensor-456");
        request.setSensorType(SensorType.TEMPERATURE_HUMIDITY);
        request.setName("HumiditySensor1");

        mockMvc.perform(post("/api/sensors/register") //경로 수정
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("모든 센서목록 조회")
    void getAllSensors() throws Exception {
        mockMvc.perform(get("/api/sensors"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("센서데이터 정상 등록")
    void saveSensorData() throws Exception {
        SensorDataRequest dataRequest = new SensorDataRequest();
        dataRequest.setDataKey(SensorDataKey.TEMPERATURE);
        dataRequest.setDataValue("25.5");

        mockMvc.perform(post("/api/sensors/" + SENSOR_UUID + "/data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dataRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("센서데이터 조회")
    void getSensorData() throws Exception {
        SensorDataRequest dataRequest = new SensorDataRequest();
        dataRequest.setDataKey(SensorDataKey.TEMPERATURE);
        dataRequest.setDataValue("25.5");

        mockMvc.perform(post("/api/sensors/" + SENSOR_UUID + "/data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dataRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/sensors/" + SENSOR_UUID + "/data"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("중복 센서 등록 실패 테스트")
    void registerDuplicateSensor() throws Exception {
        SensorRegisterRequest request = new SensorRegisterRequest();
        request.setSensorUid("sensor-123"); // 이미 등록된 UID
        request.setSensorType(SensorType.TEMPERATURE_HUMIDITY);
        request.setName("DuplicateSensor");

        mockMvc.perform(post("/api/sensors/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict()); // 409 상태 코드 기대
    }

    @Test
    @DisplayName("존재하지 않는 센서에 데이터 저장 실패")
    void saveDataToNonExistentSensor() throws Exception {
        SensorDataRequest dataRequest = new SensorDataRequest();
        dataRequest.setDataKey(SensorDataKey.TEMPERATURE);
        dataRequest.setDataValue("30.0");

        mockMvc.perform(post("/api/sensors/non-existent-sensor/data") // 잘못된 UID
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dataRequest)))
                .andExpect(status().isNotFound()); // 404 상태 코드 기대
    }

    @Test
    @DisplayName("센서 데이터 저장 후 조회 시 올바른 값 반환")
    void verifyStoredSensorData() throws Exception {
        SensorDataRequest dataRequest = new SensorDataRequest();
        dataRequest.setDataKey(SensorDataKey.TEMPERATURE);
        dataRequest.setDataValue("25.5");

        mockMvc.perform(post("/api/sensors/" + SENSOR_UUID + "/data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dataRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/sensors/" + SENSOR_UUID + "/data"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].dataKey").value("TEMPERATURE")) // 데이터 키 확인
                .andExpect(jsonPath("$[0].dataValue").value("25.5")); // 데이터 값 확인
    }

    @Test
    @DisplayName("센서 등록 시 초기 상태는 OFFLINE")
    void sensorOffline() throws Exception {
        // @BeforeEach에서 SENSOR_UUID 센서가 이미 등록됨
        Sensor sensor = sensorRepository.findBySensorUid(SENSOR_UUID)
                .orElseThrow(() -> new SensorNotFoundException("해당 UID를 가진 센서를 찾을 수 없습니다: " + SENSOR_UUID));

        // 초기 상태가 OFFLINE인지 확인
        SensorStatus offlineSensor = sensorStatusRepository.findBySensor(sensor)
                .orElseThrow(() -> new SensorNotFoundException("해당 UID를 가진 센서를 찾을 수 없습니다: " + SENSOR_UUID));

        Assertions.assertThat(offlineSensor.getSensorStatus()).isEqualTo("OFFLINE");
    }

    @Test
    @DisplayName("센서가 데이터를 보내면 ONLINE 상태로 변경")
    void sensorGoesOnlineAfterActivity() throws Exception {
        // @BeforeEach에서 센서가 이미 등록됨
        Sensor sensor = sensorRepository.findBySensorUid(SENSOR_UUID)
                .orElseThrow(() -> new SensorNotFoundException("해당 UID를 가진 센서를 찾을 수 없습니다: " + SENSOR_UUID));

        // 센서데이터 전송
        SensorDataRequest dataRequest = new SensorDataRequest();
        dataRequest.setDataKey(SensorDataKey.TEMPERATURE);
        dataRequest.setDataValue("25.5");

        mockMvc.perform(post("/api/sensors/" + SENSOR_UUID + "/data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dataRequest)))
                .andExpect(status().isOk());

        // 스케줄러 실행
        sensorStatusScheduler.updateSensorStatus();

        SensorStatus onlineSensor = sensorStatusRepository.findBySensor(sensor)
                .orElseThrow(() -> new SensorNotFoundException("해당 UID를 가진 센서를 찾을 수 없습니다: " + SENSOR_UUID));

        Assertions.assertThat(onlineSensor.getSensorStatus()).isEqualTo("ONLINE");
    }
}

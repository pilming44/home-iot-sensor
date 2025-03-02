package com.pilming.iot_sensor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pilming.iot_sensor.dto.SensorDataRequest;
import com.pilming.iot_sensor.dto.SensorRegisterRequest;
import jakarta.transaction.Transactional;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class SensorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String sensorUid;

    @BeforeEach
    void setUp() throws Exception {
        SensorRegisterRequest sensorRequest = new SensorRegisterRequest();
        sensorRequest.setSensorUid("sensor-123");
        sensorRequest.setSensorType("temperature");
        sensorRequest.setName("TempSensor1");

        ResultActions result = mockMvc.perform(post("/api/sensors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sensorRequest)))
                .andExpect(status().isOk());

        sensorUid = "sensor-123";
    }

    @Test
    @DisplayName("센서 정상 등록")
    void registerSensor() throws Exception {
        SensorRegisterRequest request = new SensorRegisterRequest();
        request.setSensorUid("sensor-456");
        request.setSensorType("humidity");
        request.setName("HumiditySensor1");

        mockMvc.perform(post("/api/sensors")
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
        dataRequest.setKey("temperature");
        dataRequest.setValue("25.5");

        mockMvc.perform(post("/api/sensors/" + sensorUid + "/data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dataRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("센서데이터 조회")
    void getSensorData() throws Exception {
        // 데이터 추가
        SensorDataRequest dataRequest = new SensorDataRequest();
        dataRequest.setKey("temperature");
        dataRequest.setValue("25.5");

        mockMvc.perform(post("/api/sensors/" + sensorUid + "/data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dataRequest)))
                .andExpect(status().isOk());

        // 센서 데이터 조회
        mockMvc.perform(get("/api/sensors/" + sensorUid + "/data"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}

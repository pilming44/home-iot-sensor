package com.pilming.iot_sensor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pilming.iot_sensor.dto.SensorChartResponseDto;
import com.pilming.iot_sensor.dto.SensorDataRequest;
import com.pilming.iot_sensor.dto.SensorRegisterRequest;
import com.pilming.iot_sensor.enums.SensorDataKey;
import com.pilming.iot_sensor.enums.SensorType;
import com.pilming.iot_sensor.monitoring.SensorStatusScheduler;
import com.pilming.iot_sensor.repository.SensorRepository;
import com.pilming.iot_sensor.repository.SensorStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SensorControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SensorRepository sensorRepository;
    @Autowired
    private SensorStatusRepository sensorStatusRepository;
    @Autowired
    private SensorStatusScheduler sensorStatusScheduler;

    private static final String BASE_URL = "/api/sensors";
    private static final String TEST_UID = "sensor-123";

    @BeforeEach
    void setup() throws Exception {
        // 테스트 전 항상 DB 초기화
        sensorRepository.deleteAll();

        // 기본 센서 하나 등록
        SensorRegisterRequest req = new SensorRegisterRequest();
        req.setSensorUid(TEST_UID);
        req.setSensorType(SensorType.TEMPERATURE_HUMIDITY);
        req.setName("TempSensor1");
        req.setTransmissionInterval(60);
        mockMvc.perform(post(BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("새로운 센서 등록 - 정상 케이스")
    void registerSensor_success() throws Exception {
        SensorRegisterRequest request = new SensorRegisterRequest();
        request.setSensorUid("new-sensor-001");
        request.setSensorType(SensorType.TEMPERATURE_HUMIDITY);
        request.setName("NewSensor");
        request.setTransmissionInterval(120);

        mockMvc.perform(post(BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        assertThat(sensorRepository.existsBySensorUid("new-sensor-001")).isTrue();
    }

    @Test
    @DisplayName("중복 센서 UID 등록 - 409 Conflict 응답")
    void registerSensor_duplicateUid() throws Exception {
        SensorRegisterRequest request = new SensorRegisterRequest();
        request.setSensorUid(TEST_UID);
        request.setSensorType(SensorType.TEMPERATURE_HUMIDITY);
        request.setName("DupSensor");

        mockMvc.perform(post(BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString("이미 등록된 센서입니다")));
    }

    @Test
    @DisplayName("필수 값 누락된 센서 등록 - 5xx 에러")
    void registerSensor_missingRequiredFields() throws Exception {
        SensorRegisterRequest request = new SensorRegisterRequest();
        request.setSensorUid("sensor-no-type");
        // sensorType 누락

        mockMvc.perform(post(BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("센서 목록 조회 - 센서 없음 (빈 리스트 응답)")
    void getAllSensors_whenNone() throws Exception {
        sensorRepository.deleteAll();

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("센서 목록 조회 - 여러 센서 존재")
    void getAllSensors_multiple() throws Exception {
        // 추가 센서 2개 등록
        for (int i = 1; i <= 2; i++) {
            SensorRegisterRequest req = new SensorRegisterRequest();
            req.setSensorUid("sensor-" + i);
            req.setSensorType(SensorType.TEMPERATURE_HUMIDITY);
            req.setName("Sensor" + i);
            mockMvc.perform(post(BASE_URL + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].sensorUid").value(TEST_UID));
    }

    @Test
    @DisplayName("센서 데이터 저장 및 조회 - 정상 플로우")
    void saveAndGetSensorData_success() throws Exception {
        SensorDataRequest dataReq = new SensorDataRequest();
        dataReq.setDataKey(SensorDataKey.TEMPERATURE);
        dataReq.setDataValue("23.5");

        mockMvc.perform(post(BASE_URL + "/" + TEST_UID + "/data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dataReq)))
                .andExpect(status().isOk());

        mockMvc.perform(get(BASE_URL + "/" + TEST_UID + "/data"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].dataKey").value("TEMPERATURE"))
                .andExpect(jsonPath("$[0].dataValue").value("23.5"));
    }

    @Test
    @DisplayName("센서 데이터 조회 - 데이터 없음")
    void getSensorData_noData() throws Exception {
        mockMvc.perform(get(BASE_URL + "/" + TEST_UID + "/data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("존재하지 않는 센서의 데이터 저장 시 404 오류")
    void saveSensorData_sensorNotFound() throws Exception {
        String invalidUid = "non-existent-001";
        SensorDataRequest dataReq = new SensorDataRequest();
        dataReq.setDataKey(SensorDataKey.TEMPERATURE);
        dataReq.setDataValue("30.0");

        mockMvc.perform(post(BASE_URL + "/" + invalidUid + "/data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dataReq)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("센서 데이터 저장 - 잘못된 JSON 요청")
    void saveSensorData_invalidJson() throws Exception {
        String invalidJson = "{\"dataKey\":\"INVALID_KEY\",\"dataValue\":\"50\"}";

        mockMvc.perform(post(BASE_URL + "/" + TEST_UID + "/data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("센서 차트 데이터 - 특정 센서 데이터 없는 경우 빈 데이터셋 반환")
    void getChartData_noDataForSensor() throws Exception {
        String newUid = "sensor-no-data";
        SensorRegisterRequest newSensorReq = new SensorRegisterRequest();
        newSensorReq.setSensorUid(newUid);
        newSensorReq.setSensorType(SensorType.TEMPERATURE_HUMIDITY);
        newSensorReq.setName("NoDataSensor");
        mockMvc.perform(post(BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newSensorReq)))
                .andExpect(status().isOk());

        mockMvc.perform(get(BASE_URL + "/chart-data").param("sensorUid", newUid))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.datasets").isArray())
                .andExpect(jsonPath("$.datasets.length()").value(0));
    }

    @Test
    @DisplayName("센서 차트 데이터 - 여러 센서 종합 데이터")
    void getChartData_multipleSensors() throws Exception {
        // sensorX에 데이터 저장
        SensorDataRequest dataX = new SensorDataRequest();
        dataX.setDataKey(SensorDataKey.TEMPERATURE);
        dataX.setDataValue("20.0");
        mockMvc.perform(post(BASE_URL + "/" + TEST_UID + "/data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dataX)))
                .andExpect(status().isOk());

        // sensorY 등록 및 데이터 저장
        String sensorY = "sensor-XYZ";
        SensorRegisterRequest reqY = new SensorRegisterRequest();
        reqY.setSensorUid(sensorY);
        reqY.setSensorType(SensorType.TEMPERATURE_HUMIDITY);
        reqY.setName("SensorXYZ");
        mockMvc.perform(post(BASE_URL + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqY)))
                .andExpect(status().isOk());
        SensorDataRequest dataY = new SensorDataRequest();
        dataY.setDataKey(SensorDataKey.HUMIDITY);
        dataY.setDataValue("45.0");
        mockMvc.perform(post(BASE_URL + "/" + sensorY + "/data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dataY)))
                .andExpect(status().isOk());

        MvcResult result = mockMvc.perform(get(BASE_URL + "/chart-data"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.datasets.length()", greaterThanOrEqualTo(2)))
                .andReturn();

        String json = result.getResponse().getContentAsString();
        SensorChartResponseDto chart = objectMapper.readValue(json, SensorChartResponseDto.class);
        List<SensorChartResponseDto.DatasetDto> datasets = chart.getDatasets();
        assertThat(datasets)
                .extracting(SensorChartResponseDto.DatasetDto::getLabel)  // String 타입으로 추출
                .anyMatch(label -> label.startsWith("TempSensor1"));
        assertThat(datasets)
                .extracting(SensorChartResponseDto.DatasetDto::getLabel)
                .anyMatch(label -> label.startsWith("SensorXYZ"));
    }

    @Test
    @DisplayName("센서 차트 데이터 - 미래 기간 필터 시 빈 데이터셋 반환")
    void getChartData_withDateRange() throws Exception {
        mockMvc.perform(get(BASE_URL + "/chart-data")
                        .param("sensorUid", TEST_UID)
                        .param("from", "2099-01-01T00:00:00")
                        .param("to", "2099-12-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.datasets.length()").value(0));
    }

    @Test
    @DisplayName("센서 등록 직후 초기 상태는 OFFLINE")
    void getSensorStatus_initialOffline() throws Exception {
        // @BeforeEach 에서 이미 센서가 등록되어 있고, SensorStatusScheduler 에 의해 OFFLINE 으로 초기화 되어 있어야 함
        mockMvc.perform(get(BASE_URL + "/" + TEST_UID + "/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.sensorStatus").value("OFFLINE"));
    }

    @Test
    @DisplayName("센서가 데이터를 보내면 ONLINE 상태로 변경")
    void sensorGoesOnlineAfterActivity_viaStatusEndpoint() throws Exception {
        // 1) 센서 데이터 전송
        SensorDataRequest dataRequest = new SensorDataRequest();
        dataRequest.setDataKey(SensorDataKey.TEMPERATURE);
        dataRequest.setDataValue("25.5");
        mockMvc.perform(post(BASE_URL + "/" + TEST_UID + "/data")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dataRequest)))
                .andExpect(status().isOk());

        // 2) 스케줄러 실행 (실제 운영에서는 주기 실행)
        sensorStatusScheduler.updateSensorStatus();

        // 3) 컨트롤러 상태 조회 API 로 ONLINE 확인
        mockMvc.perform(get(BASE_URL + "/" + TEST_UID + "/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.sensorStatus").value("ONLINE"));
    }
}

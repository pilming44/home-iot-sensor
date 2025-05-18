package com.pilming.iot_sensor.service.assembler;

import com.pilming.iot_sensor.dto.SensorChartResponseDto;
import com.pilming.iot_sensor.dto.SensorChartResponseDto.DatasetDto;
import com.pilming.iot_sensor.entity.Sensor;
import com.pilming.iot_sensor.entity.SensorData;
import com.pilming.iot_sensor.enums.SensorDataKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SensorChartAssemblerTest {

    private SensorChartAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new SensorChartAssembler();
    }

    @Test
    @DisplayName("센서 목록 [] 및 데이터 [] 전달 시, 빈 데이터셋을 반환한다")
    void testEmptyInput() {
        // given
        List<Sensor> sensors = List.of();
        List<SensorData> allData = List.of();

        // when
        SensorChartResponseDto dto = assembler.toDto(sensors, allData);

        // then
        assertThat(dto.getDatasets()).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("단일 센서 'Sensor1'와 단일 온도 데이터 전달 시, 해당 온도 데이터가 단일 Dataset으로 반환된다")
    void testSingleSensorSingleDataType() {
        // given
        Sensor sensor = Sensor.builder().sensorUid("U1").name("Sensor1").build();
        LocalDateTime ts = LocalDateTime.of(2025, 5, 18, 10, 0, 0);
        SensorData dataPoint = SensorData.builder()
                .sensor(sensor)
                .dataKey(SensorDataKey.TEMPERATURE)
                .dataValue("25.0")
                .timestamp(ts)
                .build();

        // when
        SensorChartResponseDto dto = assembler.toDto(List.of(sensor), List.of(dataPoint));
        List<DatasetDto> datasets = dto.getDatasets();

        // then
        assertThat(datasets).hasSize(1);
        DatasetDto ds = datasets.getFirst();
        assertThat(ds.getLabel()).isEqualTo("Sensor1 TEMPERATURE");
        assertThat(ds.getTimestamps()).containsExactly("05-18 10:00:00");
        assertThat(ds.getData()).containsExactly(25.0);
    }

    @Test
    @DisplayName("단일 센서 'Sensor1'에 온도(22.5 @ 11:00)와 습도(55.0 @ 11:01) 데이터 전달 시, 각각의 Dataset이 반환된다")
    void testSingleSensorMultipleDataTypes() {
        // given
        Sensor sensor = Sensor.builder().sensorUid("U1").name("Sensor1").build();
        LocalDateTime t1 = LocalDateTime.of(2025, 5, 18, 11, 0, 0);
        LocalDateTime t2 = LocalDateTime.of(2025, 5, 18, 11, 1, 0);
        SensorData tempPoint = SensorData.builder()
                .sensor(sensor)
                .dataKey(SensorDataKey.TEMPERATURE)
                .dataValue("22.5")
                .timestamp(t1)
                .build();
        SensorData humPoint = SensorData.builder()
                .sensor(sensor)
                .dataKey(SensorDataKey.HUMIDITY)
                .dataValue("55.0")
                .timestamp(t2)
                .build();

        // when
        SensorChartResponseDto dto = assembler.toDto(List.of(sensor), List.of(tempPoint, humPoint));
        List<DatasetDto> datasets = dto.getDatasets();

        // then
        assertThat(datasets).hasSize(2);
        DatasetDto tempDs = datasets.stream().filter(d -> d.getLabel().contains("TEMPERATURE")).findFirst().orElseThrow();
        assertThat(tempDs.getTimestamps()).containsExactly("05-18 11:00:00");
        assertThat(tempDs.getData()).containsExactly(22.5);
        DatasetDto humDs = datasets.stream().filter(d -> d.getLabel().contains("HUMIDITY")).findFirst().orElseThrow();
        assertThat(humDs.getTimestamps()).containsExactly("05-18 11:01:00");
        assertThat(humDs.getData()).containsExactly(55.0);
    }

    @Test
    @DisplayName("다중 센서(SensorA, SensorB)에 각각 온도/습도 데이터 전달 시, 센서별로 구분된 Dataset를 반환한다")
    void testMultipleSensorsMultipleDataTypes() {
        // given
        Sensor s1 = Sensor.builder().sensorUid("S1").name("SensorA").build();
        Sensor s2 = Sensor.builder().sensorUid("S2").name("SensorB").build();
        LocalDateTime t1 = LocalDateTime.of(2025, 5, 18, 12, 0, 0);
        LocalDateTime t2 = LocalDateTime.of(2025, 5, 18, 12, 1, 0);
        SensorData d1 = SensorData.builder().sensor(s1).dataKey(SensorDataKey.TEMPERATURE).dataValue("20.0").timestamp(t1).build();
        SensorData d2 = SensorData.builder().sensor(s2).dataKey(SensorDataKey.HUMIDITY).dataValue("60.0").timestamp(t2).build();

        // when
        SensorChartResponseDto dto = assembler.toDto(List.of(s1, s2), List.of(d1, d2));
        List<DatasetDto> datasets = dto.getDatasets();

        // then
        assertThat(datasets).hasSize(2)
                .extracting(DatasetDto::getLabel)
                .containsExactlyInAnyOrder("SensorA TEMPERATURE", "SensorB HUMIDITY");
    }

    @Test
    @DisplayName("입력 데이터의 timestamp가 정렬되지 않은 경우에도 결과 Dataset이 시간순으로 정렬된다")
    void testUnsortedInputSorting() {
        // given
        Sensor sensor = Sensor.builder().sensorUid("U1").name("Sensor1").build();
        LocalDateTime early = LocalDateTime.of(2025, 5, 18, 9, 0, 0);
        LocalDateTime late = LocalDateTime.of(2025, 5, 18, 10, 0, 0);
        SensorData dLate = SensorData.builder().sensor(sensor).dataKey(SensorDataKey.TEMPERATURE).dataValue("30.0").timestamp(late).build();
        SensorData dEarly = SensorData.builder().sensor(sensor).dataKey(SensorDataKey.TEMPERATURE).dataValue("15.0").timestamp(early).build();

        // when
        SensorChartResponseDto dto = assembler.toDto(List.of(sensor), List.of(dLate, dEarly));
        DatasetDto ds = dto.getDatasets().getFirst();

        // then
        assertThat(ds.getTimestamps()).containsExactly("05-18 09:00:00", "05-18 10:00:00");
        assertThat(ds.getData()).containsExactly(15.0, 30.0);
    }

    @Test
    @DisplayName("timestamp가 nano 단위를 포함할 때 초 단위로 잘리며 포맷된다")
    void testTimestampTruncation() {
        // given
        Sensor sensor = Sensor.builder().sensorUid("U1").name("Sensor1").build();
        LocalDateTime t1 = LocalDateTime.of(2025, 5, 18, 13, 0, 0, 500_000_000);
        SensorData sd1 = SensorData.builder().sensor(sensor).dataKey(SensorDataKey.HUMIDITY).dataValue("40.0").timestamp(t1).build();

        // when
        SensorChartResponseDto dto = assembler.toDto(List.of(sensor), List.of(sd1));
        DatasetDto ds = dto.getDatasets().getFirst();

        // then
        assertThat(ds.getTimestamps()).containsExactly("05-18 13:00:00");
        assertThat(ds.getData()).containsExactly(40.0);
    }
}

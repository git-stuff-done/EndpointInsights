package com.vsp.endpointinsightsapi.mapper;

import com.vsp.endpointinsightsapi.dto.BatchResponseDTO;
import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.model.entity.TestBatchEmailList;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class TestBatchMapperTest {

    private BatchMapper batchMapper = Mappers.getMapper(BatchMapper.class);

    private TestBatch buildTestBatch() {
        TestBatchEmailList email1 = new TestBatchEmailList();
        email1.setEmail("test@email.com");

        TestBatchEmailList email2 = new TestBatchEmailList();
        email2.setEmail("test2@email.com");

        TestBatch batch = new TestBatch();
        batch.setBatchId(UUID.randomUUID());
        batch.setBatchName("Test Batch");
        batch.setScheduleId(1L);
        batch.setStartTime(LocalDateTime.now());
        batch.setLastTimeRun(LocalDateTime.now());
        batch.setActive(true);
        batch.setCronExpression("0 0 12 * * *");
        batch.setJobs(List.of());
        batch.setNotificationList(List.of(email1, email2));
        return batch;
    }

    @Test
    void toDto_mapsAllFields() {
        TestBatch batch = buildTestBatch();
        BatchResponseDTO dto = batchMapper.toDto(batch);

        assertThat(dto.getId()).isEqualTo(batch.getBatchId());
        assertThat(dto.getBatchName()).isEqualTo(batch.getBatchName());
        assertThat(dto.getScheduleId()).isEqualTo(batch.getScheduleId());
        assertThat(dto.getStartTime()).isEqualTo(batch.getStartTime());
        assertThat(dto.getLastTimeRun()).isEqualTo(batch.getLastTimeRun());
        assertThat(dto.getCronExpression()).isEqualTo(batch.getCronExpression());
    }

    @Test
    void toDto_mapsEmailsFromNotificationList() {
        TestBatch batch = buildTestBatch();
        BatchResponseDTO dto = batchMapper.toDto(batch);

        assertThat(dto.getNotificationList())
                .containsExactlyInAnyOrder("test@email.com", "test2@email.com");
    }

    @Test
    void toDto_nullNotificationList_returnsEmptyList() {
        TestBatch batch = buildTestBatch();
        batch.setNotificationList(null);

        BatchResponseDTO dto = batchMapper.toDto(batch);

        assertThat(dto.getNotificationList()).isEmpty();
    }

    @Test
    void toDto_emptyNotificationList_returnsEmptyList() {
        TestBatch batch = buildTestBatch();
        batch.setNotificationList(List.of());

        BatchResponseDTO dto = batchMapper.toDto(batch);

        assertThat(dto.getNotificationList()).isEmpty();
    }

    @Test
    void toDto_mapsIdFromBatchId() {
        TestBatch batch = buildTestBatch();
        UUID expectedId = UUID.randomUUID();
        batch.setBatchId(expectedId);

        BatchResponseDTO dto = batchMapper.toDto(batch);

        assertThat(dto.getId()).isEqualTo(expectedId);
    }
}

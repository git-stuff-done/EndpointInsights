// BatchServiceTest.java
package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.dto.BatchRequestDTO;
import com.vsp.endpointinsightsapi.dto.BatchResponseDTO;
import com.vsp.endpointinsightsapi.exception.BatchNotFoundException;
import com.vsp.endpointinsightsapi.mapper.BatchMapper;
import com.vsp.endpointinsightsapi.model.BatchNotificationListUserId;
import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.repository.BatchNotificationListIdsRepository;
import com.vsp.endpointinsightsapi.repository.JobRepository;
import com.vsp.endpointinsightsapi.repository.TestBatchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchServiceTest {

    @Mock TestBatchRepository testBatchRepository;
    @Mock BatchMapper batchMapper;
    @InjectMocks BatchService batchService;
    @Mock JobRepository jobRepository;

    @Mock
    private BatchNotificationListIdsRepository batchNotificationListIdsRepository;

    @Test
    void getBatchById_returnsDto() {
        UUID id = UUID.randomUUID();
        TestBatch entity = new TestBatch();
        // adjust getters/setters to your model
        entity.setBatch_id(id);
        entity.setBatchName("Example");
        entity.setScheduleId(1001L);
        entity.setStartTime(LocalDate.parse("2025-11-08").atStartOfDay());
        entity.setLastTimeRun(LocalDate.parse("2025-11-09").atStartOfDay());
        entity.setActive(true);

        when(testBatchRepository.findById(id)).thenReturn(Optional.of(entity));

        BatchResponseDTO dto = BatchResponseDTO.builder()
                .id(id)
                .batchName("Example")
                .scheduleId(1001L)
                .startTime(LocalDate.parse("2025-11-08").atStartOfDay())
                .lastTimeRun(LocalDate.parse("2025-11-09").atStartOfDay())
                .active(true)
                .build();
        when(batchMapper.toDto(entity)).thenReturn(dto);

        BatchResponseDTO out = batchService.getBatchById(id);

        assertEquals(id, out.getId());
        assertEquals("Example", out.getBatchName());
        verify(testBatchRepository).findById(id);
        verify(batchMapper).toDto(entity);
    }

    @Test
    void getBatchById_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(testBatchRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(BatchNotFoundException.class, () -> batchService.getBatchById(id));
    }

    @Test
    void deleteBatchById_Exists() {
        UUID id = UUID.randomUUID();
        when(testBatchRepository.existsById(id)).thenReturn(true);

        batchService.deleteBatchById(id);

        verify(testBatchRepository).deleteById(id);
    }

    @Test
    void deleteBatchById_NotFound() {
        UUID id = UUID.randomUUID();
        when(testBatchRepository.existsById(id)).thenReturn(false);

        assertThrows(BatchNotFoundException.class, () -> batchService.deleteBatchById(id));
    }


    @Test
    void get_all_match_by_criteria(){
        UUID batchId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        TestBatch batch = new TestBatch();
        batch.setBatch_id(batchId);
        batch.setBatchName("Test Batch");

        BatchResponseDTO dto = new BatchResponseDTO();
        dto.setId(batchId);
        dto.setBatchName("Test Batch");

        BatchNotificationListUserId notification = new BatchNotificationListUserId();
        notification.setUserId(userId);
        dto.setNotificationList(List.of(userId));

        when(testBatchRepository.findAllByCriteria("", null)).thenReturn(List.of(batch));
        when(batchNotificationListIdsRepository.findAllByBatchId(batchId)).thenReturn(List.of(notification));
        when(batchMapper.toDto(batch)).thenReturn(dto);
        List<BatchResponseDTO> result = batchService.getAllBatchesByCriteria("", null);

        System.out.println(result);
        assertEquals(1, result.size());
        assertEquals("Test Batch", result.get(0).getBatchName());
        assertEquals(1, result.get(0).getNotificationList().size());
    }


    @Test
    void update_batch(){
        UUID id = UUID.randomUUID();
        TestBatch batch = new TestBatch();
        batch.setBatch_id(id);
        batch.setBatchName("Test Batch");
        batch.setScheduleId(1001L);
        batch.setNotificationList(List.of(id));

        BatchRequestDTO dto = new BatchRequestDTO();
        dto.setId(id);
        dto.setBatchName("Test Batch");
        dto.setScheduleId(1001L);
        dto.setNotificationList(List.of(id));


        BatchResponseDTO dto2 = new BatchResponseDTO();
        dto2.setId(id);
        dto2.setBatchName("Test Batch");
        dto2.setScheduleId(1001L);
        dto2.setNotificationList(List.of(id));

        when(testBatchRepository.findById(batch.getBatch_id())).thenReturn(Optional.of(batch));
        when(batchNotificationListIdsRepository.deleteAllByBatchId(batch.getBatch_id())).thenReturn(List.of());
        when(batchMapper.toDto(batch)).thenReturn(dto2);
        when(testBatchRepository.save(any(TestBatch.class)))
                .thenReturn(batch);

        BatchResponseDTO b = batchService.updateBatch(dto);
        verify(testBatchRepository).findById(batch.getBatch_id());
        verify(batchNotificationListIdsRepository).deleteAllByBatchId(batch.getBatch_id());
        verify(batchMapper).toDto(batch);

        assertEquals("Test Batch", b.getBatchName());

    }

    @Test
    void delete_participants(){
        UUID batchId = UUID.randomUUID();
        List<UUID> userIds = List.of(UUID.randomUUID(), UUID.randomUUID());

        BatchNotificationListUserId u1 = new BatchNotificationListUserId();
        u1.setUserId(userIds.get(0));

        BatchNotificationListUserId u2 = new BatchNotificationListUserId();
        u2.setUserId(userIds.get(1));

        when(batchNotificationListIdsRepository.findAllByBatchId(batchId))
                .thenReturn(List.of(u1, u2));

        List<UUID> result = batchService.deleteParticipants(userIds, batchId);

        verify(batchNotificationListIdsRepository)
                .deleteByBatchIdAndUserIdIn(batchId, userIds);
        verify(batchNotificationListIdsRepository)
                .findAllByBatchId(batchId);

        assertEquals(userIds, result);
    }

}

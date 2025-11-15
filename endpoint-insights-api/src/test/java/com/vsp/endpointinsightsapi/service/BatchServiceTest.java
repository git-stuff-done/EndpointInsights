// BatchServiceTest.java
package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.dto.BatchResponseDTO;
import com.vsp.endpointinsightsapi.exception.BatchNotFoundException;
import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.repository.TestBatchRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BatchServiceTest {

    @Mock TestBatchRepository repo;
    @InjectMocks BatchService service;

    @Test
    void getBatchById_returnsDto() {
        UUID id = UUID.randomUUID();
        TestBatch entity = new TestBatch();
        // adjust getters/setters to your model
        entity.setBatch_id(id);
        entity.setBatchName("Example");
        entity.setScheduleId(1001L);
        entity.setStartTime(LocalDate.parse("2025-11-08"));
        entity.setLastTimeRun(LocalDate.parse("2025-11-09"));
        entity.setActive(true);

        when(repo.findById(id)).thenReturn(Optional.of(entity));

        BatchResponseDTO dto = service.getBatchById(id);

        assertThat(dto.getId()).isEqualTo(id);
        assertThat(dto.getBatchName()).isEqualTo("Example");
        assertThat(dto.getScheduleId()).isEqualTo(1001L);
        assertThat(dto.getActive()).isTrue();
    }

    @Test
    void getBatchById_notFound_throws() {
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.empty());

        assertThrows(BatchNotFoundException.class, () -> service.getBatchById(id));
    }
}

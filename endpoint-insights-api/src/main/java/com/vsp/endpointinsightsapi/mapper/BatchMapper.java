package com.vsp.endpointinsightsapi.mapper;

import com.vsp.endpointinsightsapi.dto.BatchResponseDTO;
import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.model.entity.TestBatchEmailList;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BatchMapper extends AuditedMapper {

    private final JobMapper jobMapper;

    public BatchMapper(JobMapper jobMapper) {
        this.jobMapper = jobMapper;
    }

    public BatchResponseDTO toDto(TestBatch testBatch) {
        if (testBatch == null) {
            return null;
        }

        BatchResponseDTO dto = new BatchResponseDTO();
        dto.setId(testBatch.getBatchId());
        dto.setBatchName(testBatch.getBatchName());
        dto.setScheduleId(testBatch.getScheduleId());
        dto.setStartTime(testBatch.getStartTime());
        dto.setLastTimeRun(testBatch.getLastTimeRun());
        dto.setActive(testBatch.getActive());
        dto.setCronExpression(testBatch.getCronExpression());
        dto.setNotificationList(mapEmails(testBatch.getNotificationList()));

        if (testBatch.getJobs() != null) {
            dto.setJobs(testBatch.getJobs().stream()
                    .map(jobMapper::toDTO)
                    .collect(Collectors.toList()));
        }

        mapAuditFields(testBatch, dto);

        return dto;
    }

    private List<String> mapEmails(List<TestBatchEmailList> list) {
        if (list == null) return List.of();
        return list.stream()
                .map(TestBatchEmailList::getEmail)
                .collect(Collectors.toList());
    }

}

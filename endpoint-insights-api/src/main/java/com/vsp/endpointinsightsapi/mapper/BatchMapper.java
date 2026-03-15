package com.vsp.endpointinsightsapi.mapper;

import com.vsp.endpointinsightsapi.dto.BatchResponseDTO;
import com.vsp.endpointinsightsapi.model.TestBatch;
import com.vsp.endpointinsightsapi.model.entity.TestBatchEmailList;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface BatchMapper {

    // MapStruct will generate the implementation automatically
    @Mapping(source = "batchId", target = "id")
    @Mapping(source = "batchName", target = "batchName")
    @Mapping(source = "scheduleId", target = "scheduleId")
    @Mapping(source = "startTime", target = "startTime")
    @Mapping(source = "lastTimeRun", target = "lastTimeRun")
    @Mapping(source = "active", target = "active")
    @Mapping(source = "cronExpression", target = "cronExpression")
    @Mapping(source = "jobs", target = "jobs")
    @Mapping(target = "notificationList", expression = "java(mapEmails(testBatch.getNotificationList()))")
    BatchResponseDTO toDto(TestBatch testBatch);

    default List<String> mapEmails(List<TestBatchEmailList> list) {
        if (list == null) return List.of();
        return list.stream()
                .map(TestBatchEmailList::getEmail)
                .collect(Collectors.toList());
    }

}

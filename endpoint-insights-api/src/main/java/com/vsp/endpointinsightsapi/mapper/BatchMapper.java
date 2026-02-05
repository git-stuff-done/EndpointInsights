package com.vsp.endpointinsightsapi.mapper;

import com.vsp.endpointinsightsapi.dto.BatchResponseDTO;
import com.vsp.endpointinsightsapi.model.TestBatch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BatchMapper {

    // MapStruct will generate the implementation automatically
    @Mapping(source = "batch_id", target = "id")
    BatchResponseDTO toDto(TestBatch entity);
}

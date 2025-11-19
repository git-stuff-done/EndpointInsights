package com.vsp.endpointinsightsapi.mapper;

import com.vsp.endpointinsightsapi.dto.BatchResponseDTO;
import com.vsp.endpointinsightsapi.model.TestBatch;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BatchMapper {

    // MapStruct will generate the implementation automatically
    BatchResponseDTO toDto(TestBatch entity);
}

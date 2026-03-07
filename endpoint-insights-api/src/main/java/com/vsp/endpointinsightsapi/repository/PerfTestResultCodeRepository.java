package com.vsp.endpointinsightsapi.repository;

import com.vsp.endpointinsightsapi.model.entity.PerfTestResultCode;
import com.vsp.endpointinsightsapi.model.entity.PerfTestResultCodeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PerfTestResultCodeRepository extends JpaRepository<PerfTestResultCode, PerfTestResultCodeId> {

	List<PerfTestResultCode> findAllByIdResultId(UUID resultId);
	List<PerfTestResultCode> findAllByIdErrorCode(Integer errorCode);
}

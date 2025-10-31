package com.vsp.endpointinsightsapi.repository;

import com.vsp.endpointinsightsapi.model.entity.PerfTestResultCode;
import com.vsp.endpointinsightsapi.model.entity.PerfTestResultCodeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PerfTestResultCodeRepository extends JpaRepository<PerfTestResultCode, PerfTestResultCodeId> {

	List<PerfTestResultCode> findAllByResultId(Integer runId);
	List<PerfTestResultCode> findAllByIdErrorCode(Integer errorCode);
}

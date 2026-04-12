package com.vsp.endpointinsightsapi.repository;
import com.vsp.endpointinsightsapi.model.Job;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID> {


    /**
     * Find a job by id with creator and updater eagerly fetched.
     */
    @Query("SELECT j FROM Job j " +
            "LEFT JOIN FETCH j.creator " +
            "LEFT JOIN FETCH j.updater " +
            "WHERE j.jobId = :jobId")
    Optional<Job> findByIdWithUsers(@Param("jobId") UUID jobId);

    /**
     * Find all jobs with creator and updater eagerly fetched.
     */
    @Query("SELECT DISTINCT j FROM Job j " +
            "LEFT JOIN FETCH j.creator " +
            "LEFT JOIN FETCH j.updater")
    List<Job> findAllWithUsers();
}
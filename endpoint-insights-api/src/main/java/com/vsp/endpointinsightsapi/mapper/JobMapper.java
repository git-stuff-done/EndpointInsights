package com.vsp.endpointinsightsapi.mapper;

import com.vsp.endpointinsightsapi.dto.JobDTO;
import com.vsp.endpointinsightsapi.model.Job;
import org.springframework.stereotype.Component;

@Component
public class JobMapper extends AuditedMapper {

    public JobDTO toDTO(Job job) {
        if (job == null) {
            return null;
        }

        JobDTO dto = new JobDTO();
        dto.setJobId(job.getJobId());
        dto.setName(job.getName());
        dto.setDescription(job.getDescription());
        dto.setGitUrl(job.getGitUrl());
        dto.setStatus(job.getStatus());
        dto.setGitAuthType(job.getGitAuthType());
        dto.setRunCommand(job.getRunCommand());
        dto.setJmeterTestName(job.getJmeterTestName());
        dto.setCompileCommand(job.getCompileCommand());
        dto.setJobType(job.getJobType());
        dto.setConfig(job.getConfig());
        dto.setThreshold(job.getThreshold());

        mapAuditFields(job, dto);

        return dto;
    }
}
package com.vsp.endpointinsightsapi.runner;

import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class GitService {
    public File cloneRepository(String gitUrl, String jobId, String jobName) throws IOException {
        if (gitUrl == null || gitUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("No git URL provided for job: " + jobName);
        }

        File tmpRoot = new File("tmp");
        if (!tmpRoot.exists() && !tmpRoot.mkdirs()) {
                throw new IOException("Could not create tmp root directory");
        }

        // Create unique directory for this job
        String jobSubDirectoryName = "job_" + jobId + "_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));

        File tempDir = new File(tmpRoot, jobSubDirectoryName);
        if (!tempDir.mkdirs()) {
            throw new IOException("Could not create temporary job directory: " + tempDir.getAbsolutePath());
        }

        ProcessBuilder processBuilder = new ProcessBuilder("git", "clone", gitUrl);
        processBuilder.directory(tempDir);

        try {
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("Git clone failed with exit code: " + exitCode + " for job: " + jobName);
            }
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Git clone interrupted for job: " + jobName + ": " + e.getMessage());
        }

        return tempDir;
    }
}

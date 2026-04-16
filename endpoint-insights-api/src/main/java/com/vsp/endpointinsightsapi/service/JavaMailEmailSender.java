package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.model.Job;
import com.vsp.endpointinsightsapi.model.entity.PerfTestResult;
import com.vsp.endpointinsightsapi.model.entity.TestResult;
import com.vsp.endpointinsightsapi.model.entity.TestRun;
import com.vsp.endpointinsightsapi.model.enums.JobStatus;
import com.vsp.endpointinsightsapi.model.enums.TestFailureTypes;
import com.vsp.endpointinsightsapi.repository.JobRepository;
import com.vsp.endpointinsightsapi.repository.PerfTestResultRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Profile("!EMAIL-NOOP")
@Slf4j
public class JavaMailEmailSender implements EmailSender {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String from;

    private final ResourceLoader resourceLoader;
    private final JobRepository jobRepository;

    private static final String FAILURE_ROW_TEMPLATE = """
    <div style="border: 1px solid #ddd; border-radius: 4px; padding: 12px; margin-bottom: 12px; background-color: #fff;">
        <p><span style="font-size: 16px; font-weight: bold; color: #cc0000;">Job: {jobName}</span></p>
        <p><span style="font-size: 14px; font-weight: bold;">Reason: </span><span style="font-size: 14px;">{reason}</span></p>
        <p><span style="font-size: 14px; font-weight: bold;">P50: </span><span style="font-size: 14px;">{P50}</span>
           <span style="font-size: 14px; font-weight: bold; margin-left: 10px;">P95: </span><span style="font-size: 14px;">{P95}</span>
           <span style="font-size: 14px; font-weight: bold; margin-left: 10px;">P99: </span><span style="font-size: 14px;">{P99}</span></p>
        <p><span style="font-size: 14px; font-weight: bold;">Threshold: </span><span style="font-size: 14px;">{threshold}</span></p>
    </div>
    """;




    public JavaMailEmailSender(JavaMailSender mailSender, ResourceLoader resourceLoader, JobRepository jobRepository) {
        this.mailSender = mailSender;
        this.resourceLoader = resourceLoader;
        this.jobRepository = jobRepository;
    }

    @Override
    public void sendTestCompletionEmail(String batchName, TestRun testRun, String recipientEmail, List<TestResult> results) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        Resource resource = resourceLoader.getResource("classpath:email_templates/failed_test_email_template.html");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy h:mm a")
                .withZone(ZoneId.of("America/Los_Angeles"));

        Map<String, String> variablesMap = new HashMap<>();
        variablesMap.put("name", batchName);
        variablesMap.put("date", formatter.format(ZonedDateTime.now(ZoneId.of("America/Los_Angeles"))));
        variablesMap.put("startTime", formatter.format(testRun.getStartedAt()));
        variablesMap.put("endTime", formatter.format(testRun.getFinishedAt()));

        StringBuilder failureRows = new StringBuilder();
        String reasonForFailure = TestFailureTypes.OTHER.toString();

        if(results == null || results.isEmpty()){
            Optional<Job> job = jobRepository.findById(testRun.getJobId());
            String jobName = job.map(Job::getName).orElse("Unknown");
            String row = FAILURE_ROW_TEMPLATE
                    .replace("{jobName}", jobName)
                    .replace("{reason}", reasonForFailure)
                    .replace("{P50}", "-")
                    .replace("{P95}", "-")
                    .replace("{P99}", "-")
                    .replace("{threshold}", "-");

            failureRows.append(row);
        }
        else{
            for (TestResult result : results) {
                PerfTestResult perfTestResult = result.getPerfTestResult();
                if (perfTestResult == null || !perfTestResult.getLatencyThresholdResult().equals(JobStatus.FAIL.name())) continue;

                reasonForFailure = TestFailureTypes.LATENCY_THRESHOLD_EXCEEDED.toString();

                Optional<Job> job = jobRepository.findById(testRun.getJobId());
                String jobName = job.map(Job::getName).orElse("Unknown");
                String threshold = job.map(j -> j.getThreshold().toString() + "ms").orElse("N/A");

                String row = FAILURE_ROW_TEMPLATE
                        .replace("{jobName}", jobName)
                        .replace("{reason}", reasonForFailure)
                        .replace("{P50}", perfTestResult.getP50LatencyMs() + "ms")
                        .replace("{P95}", perfTestResult.getP95LatencyMs() + "ms")
                        .replace("{P99}", perfTestResult.getP99LatencyMs() + "ms")
                        .replace("{threshold}", threshold);

                failureRows.append(row);
            }
        }


        variablesMap.put("failureRows", failureRows.toString());

        String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        for (String variable : variablesMap.keySet()) {
            template = template.replace("{" + variable + "}", variablesMap.get(variable));
        }

        helper.setFrom(from);
        helper.setTo(recipientEmail);
        helper.setSubject("Test Run Completed - " + testRun.getRunId());
        helper.setText(template, true);
        mailSender.send(message);
    }

    private String buildEmailBody(UUID runId, UUID resultId) {
        String status = resultId != null ? "COMPLETED" : "FAILED";
        String resultLine = resultId != null
                ? "Result ID: " + resultId
                : "No result data available (test may have failed to execute).";
        return "Your test run has " + status + ".\n\nRun ID: " + runId + "\n" + resultLine;
    }
}

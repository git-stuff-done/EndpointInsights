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
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@Profile("!EMAIL-NOOP")
public class JavaMailEmailSender implements EmailSender {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String from;

    private final ResourceLoader resourceLoader;
    private final JobRepository jobRepository;

    public JavaMailEmailSender(JavaMailSender mailSender, ResourceLoader resourceLoader, JobRepository jobRepository) {
        this.mailSender = mailSender;
        this.resourceLoader = resourceLoader;
        this.jobRepository = jobRepository;
    }

    @Override
    public void sendTestCompletionEmail(String batchName, TestRun testRun, String recipientEmail, List<TestResult> results) throws IOException, MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        Resource resource = resourceLoader.getResource("classpath:email_templates/failed_test_email_template.html");
        Map<String, String> variablesMap = new HashMap<>();
        PerfTestResult perf = new PerfTestResult();
        String reasonForFailure = TestFailureTypes.OTHER.toString();
        for(TestResult testResult : results){
            List<PerfTestResult> perfTestResults = testResult.getPerfTestResult();
            for(PerfTestResult perfTestResult : perfTestResults){
                if(perfTestResult.getTestResult().getId().equals(testResult.getId()) && perfTestResult.getJobId().equals(testRun.getJobId()) && perfTestResult.getLatencyThresholdResult().equals(JobStatus.FAIL.name()) ){
                    perf = perfTestResult;
                    reasonForFailure = TestFailureTypes.LATENCY_THRESHOLD_EXCEEDED.toString();
                }
            }
        }
        Optional<Job> job =  jobRepository.findById(testRun.getJobId());
        if (job.isEmpty()) {
            throw new EntityNotFoundException("Job with id " + testRun.getJobId() + " not found");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy h:mm a")
                .withZone(ZoneId.of("America/Los_Angeles"));

        variablesMap.put("name", batchName);
        variablesMap.put("date", formatter.format(LocalDateTime.now()));
        variablesMap.put("startTime", formatter.format(testRun.getStartedAt()));
        variablesMap.put("endTime", formatter.format(testRun.getFinishedAt()));
        variablesMap.put("reason", reasonForFailure);
        variablesMap.put("jobName", job.get().getName());
        variablesMap.put("P50", perf.getP50LatencyMs() + "ms");
        variablesMap.put("P95", perf.getP95LatencyMs() + "ms");
        variablesMap.put("P99", perf.getP99LatencyMs() + "ms");
        variablesMap.put("threshold", job.get().getThreshold().toString() + "ms");

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

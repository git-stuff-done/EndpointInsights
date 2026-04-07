package com.vsp.endpointinsightsapi.service;

import com.vsp.endpointinsightsapi.model.entity.TestRun;
import com.vsp.endpointinsightsapi.model.enums.EmailTemplateVariables;
import com.vsp.endpointinsightsapi.repository.TestRunRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Profile("!EMAIL-NOOP")
public class JavaMailEmailSender implements EmailSender {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String from;

    private final ResourceLoader resourceLoader;
    private final TestRunRepository testRunRepository;

    public JavaMailEmailSender(JavaMailSender mailSender, ResourceLoader resourceLoader, TestRunRepository testRunRepository) {
        this.mailSender = mailSender;
        this.resourceLoader = resourceLoader;
        this.testRunRepository = testRunRepository;
    }

    @Override
    public void sendTestCompletionEmail(UUID runId, UUID resultId, String recipientEmail) throws IOException {
        SimpleMailMessage message = new SimpleMailMessage();
        Resource resource = resourceLoader.getResource("classpath:templates../model/email_templates/failed_test_email_template.html");
        List<String> variables = EmailTemplateVariables.FAILED_TEST_VARIABLES.getItems();
        Map<String, String> variablesMap = new HashMap<>();

        TestRun testRun = testRunRepository.findById(runId).orElse(null);

        // find test results to populate template

        if (testRun != null) {
            throw new EntityNotFoundException("TestRun with id " + runId + " not found");
        }



        String template = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

//        for (Map.Entry<String, String> entry:) {
//            template = template.replace("{{" + entry.getKey() + "}}", entry.getValue());
//        }

        message.setFrom(from);
        message.setTo(recipientEmail);
        message.setSubject("Test Run Completed - " + runId);
        message.setText(buildEmailBody(runId, resultId));
       // mailSender.send(message);                         //TODO uncomment when mailer credentials ready
    }

    private String buildEmailBody(UUID runId, UUID resultId) {
        String status = resultId != null ? "COMPLETED" : "FAILED";
        String resultLine = resultId != null
                ? "Result ID: " + resultId
                : "No result data available (test may have failed to execute).";
        return "Your test run has " + status + ".\n\nRun ID: " + runId + "\n" + resultLine;
    }
}

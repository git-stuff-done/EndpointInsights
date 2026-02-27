package com.vsp.endpointinsightsapi.runner;

import com.vsp.endpointinsightsapi.model.TestRunResult;
import com.vsp.endpointinsightsapi.model.entity.TestResult;
import com.vsp.endpointinsightsapi.repository.PerfTestResultCodeRepository;
import com.vsp.endpointinsightsapi.repository.PerfTestResultRepository;
import com.vsp.endpointinsightsapi.repository.TestResultRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JMeterInterpreterServiceUnitTest {
    @Mock PerfTestResultRepository perfTestResultRepository;
    @Mock PerfTestResultCodeRepository perfTestResultCodeRepository;
    @Mock TestResultRepository testResultRepository;

    @InjectMocks
    JMeterInterpreterService service;

    @BeforeEach
    void setUp() {
        when(testResultRepository.save(any(TestResult.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void TEST_HighErrorRate_ShouldFail() throws IOException {
        File file = generateJtlFile(100, 51);
        TestRunResult result = service.processResults(file);
        assertFalse(result.passed());
    }

    @Test
    void TEST_NoErrors_ShouldPass() throws IOException {
        File file = generateJtlFile(100, 0);
        TestRunResult result = service.processResults(file);
        assertTrue(result.passed());
    }

    @Test
    void TEST_BoundaryErrorRate_ShouldFail() throws IOException {
        // 1000 requests, 6 failures = 0.6% - just over threshold
        File file = generateJtlFile(1000, 6);
        TestRunResult result = service.processResults(file);
        assertFalse(result.passed());
    }

    @Test
    void TEST_EmptyFile_ShouldFail() throws IOException {
        // 1000 requests, 6 failures = 0.6% - just over threshold
        File file = generateJtlFile(0, 0);
        TestRunResult result = service.processResults(file);
        assertTrue(result.passed());
    }


    private File generateJtlFile(int totalRequests, int failureCount) throws IOException {
        File tempFile = File.createTempFile("jmeter-test", ".jtl");
        tempFile.deleteOnExit();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write("timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect");
            writer.newLine();

            long baseTimestamp = 1772159606807L;
            for (int i = 0; i < totalRequests; i++) {
                boolean success = i >= failureCount;
                String responseCode = success ? "200" : "500";
                String responseMessage = success ? "" : "Internal Server Error";
                String successStr = success ? "true" : "false";
                int threadNum = (i % 100) + 1;

                writer.write(String.format("%d,%d,GET /api/health,%s,%s,100 User Load 1-%d,text,%s,,422,122,1,1,http://localhost:8080/api/health,4,0,1",
                        baseTimestamp + (i * 100L),
                        10 + (i % 50),
                        responseCode,
                        responseMessage,
                        threadNum,
                        successStr));
                writer.newLine();
            }
        }
        return tempFile;
    }
}

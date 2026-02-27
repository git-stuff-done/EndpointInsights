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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
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
        // 1000 requests, 6 failures = 0.6%
        File file = generateJtlFile(1000, 6);
        TestRunResult result = service.processResults(file);
        assertFalse(result.passed());
    }

    @Test
    void TEST_ExactBoundaryErrorRate_ShouldPass() throws IOException {
        // 1000 requests, 5 failures = exactly 0.5%
        File file = generateJtlFile(1000, 5);
        TestRunResult result = service.processResults(file);
        assertTrue(result.passed());
    }

    @Test
    void TEST_EmptyFileWithHeader_ShouldPass() throws IOException {
        File file = generateJtlFile(0, 0);
        TestRunResult result = service.processResults(file);
        assertTrue(result.passed());
    }

    @Test
    void TEST_EmptyFile_ShouldFail() throws IOException {
        File tempFile = File.createTempFile("jmeter-empty", ".jtl");
        tempFile.deleteOnExit();

        assertThrows(IOException.class, () -> service.processResults(tempFile));
    }

    @Test
    void TEST_ResultId_IsAlwaysPopulated() throws IOException {
        File file = generateJtlFile(10, 0);
        TestRunResult result = service.processResults(file);
        assertNotNull(result.resultId());
    }

    @Test
    void TEST_MultipleThreadGroups_EvaluatedIndependently() throws IOException {
        File tempFile = File.createTempFile("jmeter-multi", ".jtl");
        tempFile.deleteOnExit();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write("timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Latency,IdleTime,Connect");
            writer.newLine();

            long ts = 1000000L;
            // Group A: all passing
            for (int i = 0; i < 100; i++) {
                writer.write(ts + "," + 10 + ",GET /health,200,,GroupA-1,text,true,,100,50,1,1,http://localhost/health,4,0,1");
                writer.newLine();
            }
            // Group B: 60% failure rate
            for (int i = 0; i < 100; i++) {
                boolean fail = i < 60;
                writer.write(ts + "," + 10 + ",GET /orders," + (fail ? "500" : "200") + ",," + "GroupB-1" + ",text," + (!fail) + ",,100,50,1,1,http://localhost/orders,4,0,1");
                writer.newLine();
            }
        }

        TestRunResult result = service.processResults(tempFile);
        assertFalse(result.passed());
    }

    @Test
    void TEST_Repositories_AreCalled() throws IOException {
        File file = generateJtlFile(10, 0);
        service.processResults(file);

        verify(testResultRepository).save(any(TestResult.class));
        verify(perfTestResultRepository).saveAll(anyList());
        verify(perfTestResultCodeRepository).saveAll(anyList());
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

package com.vsp.endpointinsightsapi.runner;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.event.annotation.AfterTestClass;
import org.springframework.test.context.event.annotation.BeforeTestClass;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(value = MockitoExtension.class)
class JMeterCommandEnhancerUnitTest {

    JMeterCommandService enhancer = new JMeterCommandService();

    @TempDir
    private static File workingDirectory;

    @BeforeAll
    public static void init() throws IOException {
        File testFile = new File(workingDirectory, "test.jmx");
        assertTrue(testFile.createNewFile());

        File subDir = new File(workingDirectory, "path");
        assertTrue(subDir.mkdir());

        File subSubDir = new File(subDir, "with spaces");
        assertTrue(subSubDir.mkdir());

        File testFileInSubDir = new File(subSubDir, "test-in-sub-dir.jmx");
        assertTrue(testFileInSubDir.createNewFile());
    }

    @BeforeEach
    public void setup() {
        // mock
        enhancer.setHome("opt/jmeter");
    }

    @Test
    void TEST_JMeterCommand_InjectsResultFile() {
        String[] result = enhancer.getRunCommand(workingDirectory, "test.jmx", "new-results.jtl");

        List<String> args = Arrays.asList(result);

        // old -l flag stripped, new one injected
        assertTrue(args.stream().anyMatch(arg -> arg.contains("new-results.jtl")));

        // required saveservice flags present
        assertTrue(args.contains("-Jjmeter.save.saveservice.output_format=csv"));
        assertTrue(args.contains("-Jjmeter.save.saveservice.thread_name=true"));
    }

    @Test
    void TEST_NullJmeterHome_ThrowsIllegalState() {
        JMeterCommandService noHome = new JMeterCommandService();
        noHome.setHome(null);

        assertThrows(IllegalStateException.class,
                () -> noHome.getRunCommand(workingDirectory, "test.jmx", "out.jtl"));
    }

    @Test
    void TEST_BlankJmeterHome_ThrowsIllegalState() {
        JMeterCommandService noHome = new JMeterCommandService();
        noHome.setHome("  ");
        assertThrows(IllegalStateException.class,
                () -> noHome.getRunCommand(workingDirectory, "test.jmx", "out.jtl"));
    }

    @Test
    void TEST_ExistingOutputFormatFlag_IsReplacedNotDuplicated() {
        String[] result = enhancer.getRunCommand(workingDirectory, "test.jmx", "results.jtl");
        List<String> args = Arrays.asList(result);

        long count = args.stream()
                .filter(a -> a.startsWith("-Jjmeter.save.saveservice.output_format"))
                .count();

        assertEquals(1, count);
        assertTrue(args.contains("-Jjmeter.save.saveservice.output_format=csv"));
    }

    @Test
    void TEST_QuotedTestPlanPath_IsUnquotedInResult() {
        String command = "test-in-sub-dir.jmx";
        String[] result = enhancer.getRunCommand(workingDirectory, command, "results.jtl");
        List<String> args = Arrays.asList(result);

        assertTrue(args.stream().anyMatch(arg -> arg.contains("test-in-sub-dir.jmx")));
        assertFalse(args.stream().anyMatch(a -> a.contains("\"")));
    }

    @Test
    void TEST_ExecutablePath_ContainsBinJmeter() {
        String[] result = enhancer.getRunCommand(workingDirectory, "test.jmx", "results.jtl");
        // First arg should be the resolved executable
        assertTrue(result[0].contains("bin"));
        assertTrue(result[0].contains("jmeter"));
    }

    @Test
    void TEST_AllRequiredSaveServiceFlags_ArePresent() {
        String[] result = enhancer.getRunCommand(workingDirectory, "test.jmx", "results.jtl");
        List<String> args = Arrays.asList(result);

        List<String> requiredFlags = List.of(
                "-Jjmeter.save.saveservice.output_format=csv",
                "-Jjmeter.save.saveservice.successful=true",
                "-Jjmeter.save.saveservice.label=true",
                "-Jjmeter.save.saveservice.time=true",
                "-Jjmeter.save.saveservice.response_code=true",
                "-Jjmeter.save.saveservice.timestamp=true",
                "-Jjmeter.save.saveservice.response_message=true",
                "-Jjmeter.save.saveservice.thread_name=true",
                "-Jjmeter.save.saveservice.latency=true"
        );

        for (String flag : requiredFlags) {
            assertTrue(args.contains(flag), "Missing required flag: " + flag);
        }
    }
}
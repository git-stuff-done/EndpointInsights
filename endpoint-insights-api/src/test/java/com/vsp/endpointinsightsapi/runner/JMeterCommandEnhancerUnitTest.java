package com.vsp.endpointinsightsapi.runner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(value = MockitoExtension.class)
class JMeterCommandEnhancerUnitTest {

    JMeterCommandEnhancer enhancer = new JMeterCommandEnhancer("/fake/jmeter");

    @Test
    void TEST_JMeterCommand_StripsExistingFlagsAndInjectsResultFile() {
        String command = "jmeter -n -t test.jmx -l old-results.jtl";
        String[] result = enhancer.enhanceRunCommand(command, "new-results.jtl");

        List<String> args = Arrays.asList(result);

        // old -l flag stripped, new one injected
        assertFalse(args.contains("old-results.jtl"));
        assertTrue(args.contains("new-results.jtl"));

        // required saveservice flags present
        assertTrue(args.contains("-Jjmeter.save.saveservice.output_format=csv"));
        assertTrue(args.contains("-Jjmeter.save.saveservice.thread_name=true"));
    }

    @Test
    void TEST_NonJmeterCommand_SplitsOnWhitespace() {
        String command = "echo hello world";
        String[] result = enhancer.enhanceRunCommand(command, "results.jtl");
        assertArrayEquals(new String[]{"echo", "hello", "world"}, result);
    }

    @Test
    void TEST_NullJmeterHome_ThrowsIllegalState() {
        JMeterCommandEnhancer noHome = new JMeterCommandEnhancer(null);
        assertThrows(IllegalStateException.class,
                () -> noHome.enhanceRunCommand("jmeter -n -t test.jmx", "out.jtl"));
    }

    @Test
    void TEST_BlankJmeterHome_ThrowsIllegalState() {
        JMeterCommandEnhancer noHome = new JMeterCommandEnhancer("   ");
        assertThrows(IllegalStateException.class,
                () -> noHome.enhanceRunCommand("jmeter -n -t test.jmx", "out.jtl"));
    }

    @Test
    void TEST_ExistingOutputFormatFlag_IsReplacedNotDuplicated() {
        String command = "jmeter -n -t test.jmx -Jjmeter.save.saveservice.output_format=xml";
        String[] result = enhancer.enhanceRunCommand(command, "results.jtl");
        List<String> args = Arrays.asList(result);

        long count = args.stream()
                .filter(a -> a.startsWith("-Jjmeter.save.saveservice.output_format"))
                .count();

        assertEquals(1, count);
        assertTrue(args.contains("-Jjmeter.save.saveservice.output_format=csv"));
    }

    @Test
    void TEST_QuotedTestPlanPath_IsUnquotedInResult() {
        String command = "jmeter -n -t \"/path/with spaces/test.jmx\"";
        String[] result = enhancer.enhanceRunCommand(command, "results.jtl");
        List<String> args = Arrays.asList(result);

        assertTrue(args.contains("/path/with spaces/test.jmx"));
        assertFalse(args.stream().anyMatch(a -> a.contains("\"")));
    }

    @Test
    void TEST_ExecutablePath_ContainsBinJmeter() {
        String[] result = enhancer.enhanceRunCommand("jmeter -n -t test.jmx", "results.jtl");
        // First arg should be the resolved executable
        assertTrue(result[0].contains("bin"));
        assertTrue(result[0].contains("jmeter"));
    }

    @Test
    void TEST_AllRequiredSaveServiceFlags_ArePresent() {
        String[] result = enhancer.enhanceRunCommand("jmeter -n -t test.jmx", "results.jtl");
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
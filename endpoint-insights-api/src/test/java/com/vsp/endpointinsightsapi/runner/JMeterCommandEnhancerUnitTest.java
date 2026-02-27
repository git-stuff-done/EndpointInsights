package com.vsp.endpointinsightsapi.runner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(value = MockitoExtension.class)
class JMeterCommandEnhancerUnitTest {

    JMeterCommandEnhancer enhancer = new JMeterCommandEnhancer("/fake/jmeter");

    @Test
    void testStripsExistingOutputFlagAndInjectsResultFile() {
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
}
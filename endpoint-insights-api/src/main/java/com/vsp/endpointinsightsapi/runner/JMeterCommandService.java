package com.vsp.endpointinsightsapi.runner;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
@ConfigurationProperties(prefix = "jmeter")
public class JMeterCommandService {

    private static final Logger LOG = LoggerFactory.getLogger(JMeterCommandService.class);

    @Getter @Setter
    private String home;

    private static final String[][] REQUIRED_SAVE_SERVICE_FLAGS = {
            {"jmeter.save.saveservice.output_format", "csv"},
            {"jmeter.save.saveservice.successful", "true"},
            {"jmeter.save.saveservice.label", "true"},
            {"jmeter.save.saveservice.time", "true"},
            {"jmeter.save.saveservice.response_code", "true"},
            {"jmeter.save.saveservice.timestamp", "true"},
            {"jmeter.save.saveservice.response_message", "true"},
            {"jmeter.save.saveservice.thread_name", "true"},
            {"jmeter.save.saveservice.latency", "true"},
    };

    /**
     *
     * @throws IllegalStateException on unset JMETER_HOME environment variable
     * @param testName
     * @param resultFileName
     * @return
     */
    public String[] getRunCommand(File workingDirectory, String testName, String resultFileName) {
        // 1. Get jmeter executable
        String jmeterHome = this.home;
        if (jmeterHome == null || jmeterHome.trim().isEmpty()) {
            throw new IllegalStateException("JMETER_HOME environment variable is not set.");
        }
        // Detect OS and select correct JMeter executable
        String os = System.getProperty("os.name").toLowerCase();
        String jmeterExecutable = jmeterHome + File.separator + "bin" + File.separator + "jmeter";

        // Windows compatibility for dev environments
        if (os.contains("win")) {
            jmeterExecutable += ".bat";
        }

        // confirm working directory exists
        if (workingDirectory == null || !workingDirectory.exists() || !workingDirectory.isDirectory()) {
            throw new IllegalArgumentException("Working directory does not exist or is not a directory: " + (workingDirectory != null ? workingDirectory.getAbsolutePath() : "null"));
        }

        // Recursively search for a file that matches the given test pattern
        Pattern testPattern = Pattern.compile(testName);
        File foundTestFile = findFileMatchingPattern(workingDirectory, testPattern);

        if (foundTestFile == null) {
            throw new IllegalArgumentException("No test file matching pattern '" + testName + "' was found under directory: " + workingDirectory.getAbsolutePath());
        }

        LOG.info("test file found={}", foundTestFile.getAbsolutePath());

        // Get the relative path to the test file (relative to workingDirectory)
//        String relativeTestPath = workingDirectory.toPath().relativize(foundTestFile.toPath()).toString();

        // Build arguments
        List<String> userArgs = new ArrayList<>();

        userArgs.add(jmeterExecutable);

        userArgs.add("-n");

        userArgs.add("-t");
        userArgs.add(foundTestFile.getAbsolutePath());

        userArgs.add("-l");

        String resultFileNameWithPath = workingDirectory.getAbsolutePath() + File.separator + resultFileName;

        userArgs.add(resultFileNameWithPath);

        // Ensure CSV output, and add required columns for parsing
        for (String[] flag : REQUIRED_SAVE_SERVICE_FLAGS) {
            userArgs.add("-J" + flag[0] + "=" + flag[1]);
        }

        return userArgs.toArray(String[]::new);
    }

    /**
     * Recursively searches for a file in the given directory whose name matches the pattern.
     * Returns the first match found, or null if none.
     */
    private static File findFileMatchingPattern(File dir, Pattern pattern) {
        File[] files = dir.listFiles();
        if (files == null) return null;

        for (File f : files) {
            if (f.isDirectory()) {
                File found = findFileMatchingPattern(f, pattern);
                if (found != null) return found;
            } else {
                if (pattern.matcher(f.getName()).find()) {
                    return f;
                }
            }
        }
        return null;
    }
}
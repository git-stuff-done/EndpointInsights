package com.vsp.endpointinsightsapi.runner;

import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JMeterCommandEnhancer {
    private final String jmeterHome;

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
     * Tokenizes a string by splitting on whitespace while preserving quoted values as single tokens.
     * <br>
     * Matches one of three alternatives:
     * <ul>
     *   <li>{@code [^\s"']+}    — one or more characters that are not whitespace or quotes (plain token)</li>
     *   <li>{@code "([^"]*)"} — a double-quoted string (captures inner content)</li>
     *   <li>{@code '([^']*)'} — a single-quoted string (captures inner content)</li>
     * </ul>
     */
    private static final Pattern SHELL_TOKENIZER_PATTERN = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");

    public JMeterCommandEnhancer(@Value("${jmeter.home:#{environment['JMETER_HOME']}}") String jmeterHome) {
        this.jmeterHome = jmeterHome;
    }

    /**
     *
     * @throws IllegalStateException on unset JMETER_HOME environment variable
     * @param userCommand
     * @param resultFileName
     * @return
     */
    public String[] enhanceRunCommand(String userCommand, String resultFileName) {
        if (userCommand.contains("jmeter")) {
            // 1. Get jmeter executable
            String jmeterHome = this.jmeterHome;
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

            // 2. Tokenize the command and build argument list
            //      Use regex to split by spaces except inside quotes
            List<String> userArgs = new ArrayList<>();
            // Add jmeter executable as first arg
            userArgs.add(jmeterExecutable);

            // Tokenize userCommand safely (respecting quoted testplan etc)
            Matcher matcher = SHELL_TOKENIZER_PATTERN.matcher(userCommand);

            boolean skipL = false;
            while (matcher.find()) {
                String token = matcher.group();
                if (token.equals("jmeter") || token.endsWith("jmeter")) {
                    // skip 'jmeter' itself, already added actual path above
                    continue;
                }
                // Remove any -l X (older output file param)
                if (skipL) {
                    // Skip the value after -l
                    skipL = false;
                    continue;
                }
                if (token.equals("-l")) {
                    skipL = true;
                    continue;
                }
                // Remove any jmeter.save.saveservice.* that we overwrite below
                if (isOverriddenSaveServiceFlag(token)) {
                    continue;
                }

                // Add the token, removing quotes if present
                if ((token.startsWith("\"") && token.endsWith("\"")) || (token.startsWith("'") && token.endsWith("'"))) {
                    token = token.substring(1, token.length() - 1);
                }
                userArgs.add(token);
            }

            // 3. Add our enforced result file output
            userArgs.add("-l");
            userArgs.add(resultFileName);

            // 4. Ensure CSV output, and add required columns for parsing
            for (String[] flag : REQUIRED_SAVE_SERVICE_FLAGS) {
                userArgs.add("-J" + flag[0] + "=" + flag[1]);
            }

            return userArgs.toArray(String[]::new);
        }

        // Default: just tokenize on whitespace
        return userCommand.trim().split("\\s+");
    }

    private static boolean isOverriddenSaveServiceFlag(String token) {
        for (String[] flag : REQUIRED_SAVE_SERVICE_FLAGS) {
            if (token.startsWith("-J" + flag[0])) {
                return true;
            }
        }
        return false;
    }
}
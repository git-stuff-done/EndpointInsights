package com.vsp.endpointinsightsapi.runner;

import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JMeterCommandEnhancer {
    private final String jmeterHome;

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
            Pattern pattern = Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
            Matcher matcher = pattern.matcher(userCommand);

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
                if (token.startsWith("-Jjmeter.save.saveservice.output_format")
                        || token.startsWith("-Jjmeter.save.saveservice.successful")
                        || token.startsWith("-Jjmeter.save.saveservice.label")
                        || token.startsWith("-Jjmeter.save.saveservice.time")
                        || token.startsWith("-Jjmeter.save.saveservice.response_code")
                        || token.startsWith("-Jjmeter.save.saveservice.timestamp")
                        || token.startsWith("-Jjmeter.save.saveservice.response_message")
                        || token.startsWith("-Jjmeter.save.saveservice.thread_name")
                        || token.startsWith("-Jjmeter.save.saveservice.bytes")
                        || token.startsWith("-Jjmeter.save.saveservice.latency")) {
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
            userArgs.add("-Jjmeter.save.saveservice.output_format=csv");
            userArgs.add("-Jjmeter.save.saveservice.successful=true");
            userArgs.add("-Jjmeter.save.saveservice.label=true");
            userArgs.add("-Jjmeter.save.saveservice.time=true");
            userArgs.add("-Jjmeter.save.saveservice.response_code=true");
            userArgs.add("-Jjmeter.save.saveservice.timestamp=true");
            userArgs.add("-Jjmeter.save.saveservice.response_message=true");
            userArgs.add("-Jjmeter.save.saveservice.thread_name=true");
            userArgs.add("-Jjmeter.save.saveservice.latency=true");

            return userArgs.toArray(String[]::new);
        }

        // Default: just tokenize on whitespace
        return userCommand.trim().split("\\s+");
    }
}
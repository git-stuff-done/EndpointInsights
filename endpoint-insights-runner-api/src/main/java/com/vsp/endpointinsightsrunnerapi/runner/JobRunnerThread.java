package com.vsp.endpointinsightsrunnerapi.runner;

import com.vsp.endpointinsightsapi.model.Job;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class JobRunnerThread implements Runnable {

	private final Job job;
	private final TestInterpreter testInterpreter;

	public JobRunnerThread(Job job) {
		this.job = job;

		// todo: add new interpreters as needed
		switch (job.getJobType()) {
			case PERF -> testInterpreter = new JMeterInterpreter();
			default -> testInterpreter = null;
		}

	}

	@Override
	public void run() {
		// step 1 - pull from git

		pullFromGit();

		// step 2 - compile test

		compileTest();

		// step 3 - execute test

		Optional<File> resultFile = executeTest();

		// step 4 - interpret results
		if (resultFile.isPresent()) {
			System.out.println("Test results available in: " + resultFile.get().getAbsolutePath());
			// TODO: Pass resultFile to test interpreter
		} else {
			System.out.println("No test results file available for interpretation");
		}
	}

	private String generateResultFileName() {
		String jobId = job.getJobId().toString();
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
		return "results_" + jobId + "_" + timestamp + ".jtl";
	}

	private String enhanceRunCommand(String userCommand) {
		// Generate unique result file name with timestamp
		String resultFile = generateResultFileName();

		if (userCommand.contains("jmeter")) {
			// Remove any existing -l parameter and its value
			userCommand = userCommand.replaceAll("\\s+-l\\s+\\S+", "");

			// Always add our standardized result file
			userCommand += " -l " + resultFile;

			// Ensure CSV format
			if (!userCommand.contains("jmeter.save.saveservice.output_format")) {
				userCommand += " -Jjmeter.save.saveservice.output_format=csv";
			}

			// Add essential CSV columns for consistent parsing
			userCommand += " -Jjmeter.save.saveservice.successful=true";
			userCommand += " -Jjmeter.save.saveservice.label=true";
			userCommand += " -Jjmeter.save.saveservice.time=true";
			userCommand += " -Jjmeter.save.saveservice.response_code=true";
			userCommand += " -Jjmeter.save.saveservice.timestamp=true";
			userCommand += " -Jjmeter.save.saveservice.response_message=true";
			userCommand += " -Jjmeter.save.saveservice.thread_name=true";
			userCommand += " -Jjmeter.save.saveservice.bytes=true";
			userCommand += " -Jjmeter.save.saveservice.latency=true";
		}

		return userCommand;
	}

	private void pullFromGit() {
		try {
			String gitUrl = job.getGitUrl();
			if (gitUrl != null && !gitUrl.trim().isEmpty()) {
				ProcessBuilder processBuilder = new ProcessBuilder("git", "pull", gitUrl);
				Process process = processBuilder.start();

				int exitCode = process.waitFor();
				if (exitCode == 0) {
					System.out.println("Git pull completed successfully for job: " + job.getName());
				} else {
					System.err.println("Git pull failed with exit code: " + exitCode + " for job: " + job.getName());
				}
			} else {
				System.err.println("No git URL provided for job: " + job.getName());
			}
		} catch (IOException | InterruptedException e) {
			System.err.println("Error executing git pull for job: " + job.getName() + " - " + e.getMessage());
			Thread.currentThread().interrupt();
		}
	}

	private void compileTest() {
		try {
			String compileCommand = job.getCompileCommand();
			if (compileCommand != null && !compileCommand.trim().isEmpty()) {
				String[] commandArray = compileCommand.split("\\s+");
				ProcessBuilder processBuilder = new ProcessBuilder(commandArray);
				Process process = processBuilder.start();

				int exitCode = process.waitFor();
				if (exitCode == 0) {
					System.out.println("Compilation completed successfully for job: " + job.getName());
				} else {
					System.err.println("Compilation failed with exit code: " + exitCode + " for job: " + job.getName());
				}
			} else {
				System.out.println("No compile command provided for job: " + job.getName() + " - skipping compilation");
			}
		} catch (IOException | InterruptedException e) {
			System.err.println("Error executing compile command for job: " + job.getName() + " - " + e.getMessage());
			Thread.currentThread().interrupt();
		}
	}

	private Optional<File> executeTest() {
		try {
			String originalCommand = job.getRunCommand();
			if (originalCommand != null && !originalCommand.trim().isEmpty()) {
				String enhancedCommand = enhanceRunCommand(originalCommand);
				String resultFileName = generateResultFileName();
				File resultFile = new File(resultFileName);

				System.out.println("Executing enhanced command for job: " + job.getName());
				System.out.println("Command: " + enhancedCommand);

				String[] commandArray = enhancedCommand.split("\\s+");
				ProcessBuilder processBuilder = new ProcessBuilder(commandArray);
				Process process = processBuilder.start();

				int exitCode = process.waitFor();
				if (exitCode == 0) {
					System.out.println("Test execution completed successfully for job: " + job.getName());
				} else {
					System.err.println("Test execution failed with exit code: " + exitCode + " for job: " + job.getName());
				}

				// Return the result file regardless of exit code since execution completed
				return Optional.of(resultFile);
			} else {
				System.err.println("No run command provided for job: " + job.getName());
				return Optional.empty();
			}
		} catch (IOException | InterruptedException e) {
			System.err.println("Error executing test for job: " + job.getName() + " - " + e.getMessage());
			Thread.currentThread().interrupt();
			return Optional.empty();
		}
	}
}

package com.vsp.endpointinsightsapi.runner;

import com.vsp.endpointinsightsapi.model.Job;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class JobRunnerThread implements Runnable {

	private final Job job;
	private final TestInterpreter testInterpreter;
	private File tempDir = null;

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
		// step 1 - pull from git (setup temp dir)
		try {
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
		} finally {
			cleanupTempDir();
		}
	}

	private String generateResultFileName() {
		String jobId = job.getJobId().toString();
		String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
		return "results_" + jobId + "_" + timestamp + ".jtl";
	}

	private String[] enhanceRunCommand(String userCommand) {
		// Generate unique result file name with timestamp
		String resultFile = generateResultFileName();

		if (userCommand.contains("jmeter")) {
			// 1. Get jmeter executable
			String jmeterHome = System.getenv("JMETER_HOME");
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
			List<String> userArgs = new java.util.ArrayList<>();
			// Add jmeter executable as first arg
			userArgs.add(jmeterExecutable);

			// Tokenize userCommand safely (respecting quoted testplan etc)
			java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("[^\\s\"']+|\"([^\"]*)\"|'([^']*)'");
			java.util.regex.Matcher matcher = pattern.matcher(userCommand);

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
			userArgs.add(resultFile);

			// 4. Ensure CSV output, and add required columns for parsing
			userArgs.add("-Jjmeter.save.saveservice.output_format=csv");
			userArgs.add("-Jjmeter.save.saveservice.successful=true");
			userArgs.add("-Jjmeter.save.saveservice.label=true");
			userArgs.add("-Jjmeter.save.saveservice.time=true");
			userArgs.add("-Jjmeter.save.saveservice.response_code=true");
			userArgs.add("-Jjmeter.save.saveservice.timestamp=true");
			userArgs.add("-Jjmeter.save.saveservice.response_message=true");
			userArgs.add("-Jjmeter.save.saveservice.thread_name=true");
			userArgs.add("-Jjmeter.save.saveservice.bytes=true");
			userArgs.add("-Jjmeter.save.saveservice.latency=true");

			return userArgs.toArray(String[]::new);
		}

		// Default: just tokenize on whitespace
		return userCommand.trim().split("\\s+");
	}

	private void pullFromGit() {
		try {
			String gitUrl = job.getGitUrl();
			if (gitUrl != null && !gitUrl.trim().isEmpty()) {
				// Ensure ./tmp exists
				File tmpRoot = new File("tmp");
				if (!tmpRoot.exists()) {
					boolean created = tmpRoot.mkdirs();
					if (!created) {
						throw new IOException("Could not create tmp root directory");
					}
				}
				// Create unique directory for this job
				String subdirName = "job_" + job.getJobId() + "_" +
						LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
				tempDir = new File(tmpRoot, subdirName);
				if (!tempDir.mkdirs()) {
					throw new IOException("Could not create temporary job directory: " + tempDir.getAbsolutePath());
				}

				// Clone into subdirectory (repoFolder will be inside tempDir, Git creates dirname from repo)
				ProcessBuilder processBuilder = new ProcessBuilder("git", "clone", gitUrl);
				processBuilder.directory(tempDir);
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

				// Try to find the repo directory inside tempDir
				if (tempDir != null && tempDir.exists()) {
					File[] subdirs = tempDir.listFiles(File::isDirectory);
					if (subdirs != null && subdirs.length == 1) {
						processBuilder.directory(subdirs[0]);
					} else {
						System.err.println("Could not determine repo directory under " + tempDir.getAbsolutePath());
						processBuilder.directory(tempDir); // fallback
					}
				}

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
				String[] commandArray = enhanceRunCommand(originalCommand);
				String resultFileName = generateResultFileName();
				File resultFile;

				ProcessBuilder processBuilder;
				processBuilder = new ProcessBuilder(commandArray);

				// Try to find the repo directory inside tempDir
				File workingDir = tempDir;
				if (tempDir != null && tempDir.exists()) {
					File[] subdirs = tempDir.listFiles(File::isDirectory);
					if (subdirs != null && subdirs.length == 1) {
						workingDir = subdirs[0];
						processBuilder.directory(workingDir);
					} else {
						System.err.println("Could not determine repo directory under " + tempDir.getAbsolutePath());
						processBuilder.directory(tempDir); // fallback
					}
				}
				// Place result file into working dir
				resultFile = new File(workingDir, resultFileName);

				System.out.println("Executing enhanced command for job: " + job.getName());
				System.out.println("Command: " + Arrays.stream(commandArray).reduce("", String::concat));

				Process process = processBuilder.start();

				int exitCode = process.waitFor();
				if (exitCode == 0) {
					System.out.println("Test execution completed successfully for job: " + job.getName());
				} else {
					System.err.println(process.errorReader().lines().reduce("", String::concat));
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

	private void cleanupTempDir() {
		if (tempDir != null && tempDir.exists()) {
			try {
				deleteRecursively(tempDir);
				System.out.println("Cleaned up temporary directory: " + tempDir.getAbsolutePath());
			} catch (Exception e) {
				System.err.println("Failed to clean up temporary directory: " + tempDir.getAbsolutePath() + " - " + e.getMessage());
			}
		}
	}

	private void deleteRecursively(File file) throws IOException {
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			if (files != null) {
				for (File child : files) {
					deleteRecursively(child);
				}
			}
		}
		file.setWritable(true);
		if (!file.delete()) {
			throw new IOException("Failed to delete file or directory: " + file.getAbsolutePath());
		}
	}
}

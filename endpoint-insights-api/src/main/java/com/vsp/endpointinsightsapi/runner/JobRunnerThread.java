package com.vsp.endpointinsightsapi.runner;

import com.vsp.endpointinsightsapi.model.Job;
import com.vsp.endpointinsightsapi.model.TestRunResult;
import com.vsp.endpointinsightsapi.model.entity.TestRun;
import com.vsp.endpointinsightsapi.model.enums.TestRunStatus;
import com.vsp.endpointinsightsapi.repository.TestRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Optional;

public class JobRunnerThread implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(JobRunnerThread.class);
    //TODO: add test-level logging to store as part of test results

	private final Job job;
	private final TestRun testRun;
	private final TestRunRepository testRunRepository;
	private final TestInterpreter testInterpreter;
    private final GitService gitService;
    private final JMeterCommandEnhancer jMeterCommandEnhancer;

    private File jobProjectRepoDirectory = null;

	public JobRunnerThread(Job job, TestRun testRun, TestRunRepository testRunRepository, JMeterInterpreterService jMeterInterpreterService, GitService gitService, JMeterCommandEnhancer jMeterCommandEnhancer) {
		this.job = job;
		this.testRun = testRun;
		this.testRunRepository = testRunRepository;
        this.gitService = gitService;
        this.jMeterCommandEnhancer = jMeterCommandEnhancer;

        // todo: add new interpreters as needed
		switch (job.getJobType()) {
			case PERF -> testInterpreter = jMeterInterpreterService;
			default -> testInterpreter = null;
		}
	}

	@Override
	public void run() {
		try {
            jobProjectRepoDirectory = gitService.cloneRepository(job.getGitUrl(), job.getJobId().toString(), job.getName());

            File workingDirectory = resolveWorkingDir();
			compileTest(workingDirectory);

			testRun.setStatus(TestRunStatus.RUNNING);
			testRunRepository.save(testRun);

			// step 3 - execute test
			Optional<File> testResultFile = executeTest(workingDirectory);

			// step 4 - interpret results
            if (testResultFile.isEmpty()) {
                LOG.info("No test results file available for interpretation");

                testRun.setStatus(TestRunStatus.FAILED);
                testRun.setFinishedAt(Instant.now());
                testRunRepository.save(testRun);
                return;
            }

            LOG.info("Test results available in: {}", testResultFile.get().getAbsolutePath());

            TestRunResult pass = testInterpreter.processResults(testResultFile.get());

            testRun.setStatus(pass.passed() ? TestRunStatus.COMPLETED : TestRunStatus.FAILED);
            testRun.setResultId(pass.resultId());
            testRun.setFinishedAt(Instant.now());
            testRunRepository.save(testRun);
		} catch (IOException e) {

            LOG.error("Running job failed with exception: {}", e.getMessage());
            testRun.setStatus(TestRunStatus.FAILED);
            testRun.setFinishedAt(Instant.now());
            testRunRepository.save(testRun);

        } finally {
			cleanupTempDir();
		}
	}

	private void compileTest(File workingDirectory){
		try {
			String compileCommand = job.getCompileCommand();
            if (compileCommand == null || compileCommand.trim().isEmpty()) {
                LOG.info("No compile command provided for job: {} - skipping compilation", job.getName());
                return;
            }

            String[] commandArray = compileCommand.split("\\s+");

            ProcessBuilder processBuilder = new ProcessBuilder(commandArray);
            processBuilder.directory(workingDirectory);

            Process process = processBuilder.start();

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                LOG.info("Compilation completed successfully for job: {}", job.getName());
            } else {
                LOG.error("Compilation failed with exit code: {} for job: {}", exitCode, job.getName());
            }
		} catch (IOException | InterruptedException e) {
            LOG.error("Error executing compile command for job: {} - {}", job.getName(), e.getMessage());
			Thread.currentThread().interrupt();
		}
	}

    private String generateResultFileName() {
        String jobId = job.getJobId().toString();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
        return "results_" + jobId + "_" + timestamp + ".jtl";
    }

    private File resolveWorkingDir() {
        if (jobProjectRepoDirectory == null || !jobProjectRepoDirectory.exists()) {
            return jobProjectRepoDirectory;
        }

        File[] subDirs = jobProjectRepoDirectory.listFiles(File::isDirectory);
        if (subDirs == null || subDirs.length != 1) {
            LOG.error("Could not determine repo directory under {}", jobProjectRepoDirectory.getAbsolutePath());
            return jobProjectRepoDirectory;
        }

        return subDirs[0];
    }

	private Optional<File> executeTest(File workingDirectory) {
		try {
			String originalCommand = job.getRunCommand();
            if (originalCommand == null || originalCommand.trim().isEmpty()) {
                LOG.error("No run command provided for job: {}", job.getName());
                return Optional.empty();
            }

            String resultFileName = generateResultFileName();

            File resultFile = new File(workingDirectory, resultFileName);
            String[] command = jMeterCommandEnhancer.enhanceRunCommand(originalCommand, resultFileName);

            ProcessBuilder processBuilder;
            processBuilder = new ProcessBuilder(command);

            LOG.info("Executing enhanced command for job: {}", job.getName());
            LOG.info("Command: {}", Arrays.stream(command).reduce("", String::concat));

            Process process = processBuilder.start();

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                LOG.info("Test execution completed successfully for job: {}", job.getName());
            } else {
                LOG.error(process.errorReader().lines().reduce("", String::concat));
                LOG.error("Test execution failed with exit code: {} for job: {}", exitCode, job.getName());
            }

            // Return the result file regardless of exit code since execution completed
            return Optional.of(resultFile);

		} catch (IOException | InterruptedException e) {
            LOG.error("Error executing test for job: {} - {}", job.getName(), e.getMessage());
			Thread.currentThread().interrupt();
			return Optional.empty();
		}
	}

	private void cleanupTempDir() {
        if (jobProjectRepoDirectory == null || !jobProjectRepoDirectory.exists()) {
            return;
        }

        try {
            deleteRecursively(jobProjectRepoDirectory);
            LOG.info("Cleaned up temporary directory: {}", jobProjectRepoDirectory.getAbsolutePath());
        } catch (Exception e) {
            LOG.error("Failed to clean up temporary directory: {} - {}", jobProjectRepoDirectory.getAbsolutePath(), e.getMessage());
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

        if (!file.setWritable(true)) {
            throw new IOException("Failed to delete file or directory: unable to set " + file.getAbsolutePath() + " as writable");
        }
		if (!file.delete()) {
			throw new IOException("Failed to delete file or directory: " + file.getAbsolutePath());
		}
	}
}

package com.vsp.endpointinsightsapi.runner;

import com.vsp.endpointinsightsapi.model.entity.*;
import com.vsp.endpointinsightsapi.model.enums.TestType;
import com.vsp.endpointinsightsapi.repository.PerfTestResultCodeRepository;
import com.vsp.endpointinsightsapi.repository.PerfTestResultRepository;
import com.vsp.endpointinsightsapi.repository.TestResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

@Service
public class JMeterInterpreterService implements TestInterpreter {

	private final PerfTestResultRepository perfTestResultRepository;
	private final PerfTestResultCodeRepository perfTestResultCodeRepository;
	private final TestResultRepository testResultRepository;

	// Used for interpreting test results
	private record SampleRecord(long elapsed, int responseCodeInt, String responseCode, long timeStamp, boolean success) {}

	@Autowired
	public JMeterInterpreterService(PerfTestResultRepository perfTestResultRepository, PerfTestResultCodeRepository perfTestResultCodeRepository, TestResultRepository testResultRepository) {
		this.perfTestResultRepository = perfTestResultRepository;
		this.perfTestResultCodeRepository = perfTestResultCodeRepository;
		this.testResultRepository = testResultRepository;
	}

	private int calculatePercentile(List<Long> sortedLatencies, double percentile) {
		if (sortedLatencies == null || sortedLatencies.isEmpty()) {
			return 0;
		}
		int n = sortedLatencies.size();
		double rank = percentile / 100.0 * n;
		int index = (int) Math.ceil(rank) - 1;
		if (index < 0) index = 0;
		if (index >= n) index = n - 1;
		return sortedLatencies.get(index).intValue();
	}

	@Override
	public boolean processResults(File file) throws IOException {
		// Create test result so we can get the UUID
		TestResult testResult = new TestResult();
		testResult.setId(UUID.randomUUID());
		testResult.setJobType(TestType.PERF.toInteger());
		testResult = testResultRepository.save(testResult);

		// Maps groupKey -> List of SampleRecord
		Map<String, List<SampleRecord>> grouped = new HashMap<>();
		// Maps groupKey+code -> count
		Map<String, Integer> errorCodeCount = new HashMap<>();

		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			String headerLine = br.readLine();
			if (headerLine == null) {
				throw new IOException("CSV file is empty");
			}
			String[] headers = headerLine.split(",", -1);
			Map<String, Integer> headerIndex = new HashMap<>();
			for (int i = 0; i < headers.length; i++) headerIndex.put(headers[i], i);

			String line;
			while ((line = br.readLine()) != null) {
				if (line.isEmpty()) continue;
				String[] cols = line.split(",", -1);

				String threadName = cols[headerIndex.get("threadName")];
				String threadGroup = threadName.contains("-") ? threadName.substring(0, threadName.lastIndexOf('-')) : threadName;
				String samplerNameRaw = cols[headerIndex.get("label")];
				String samplerName = samplerNameRaw.contains("-") ? samplerNameRaw.substring(0, samplerNameRaw.lastIndexOf('-')) : samplerNameRaw;
				String responseCode = cols[headerIndex.get("responseCode")];
				long elapsed, timeStamp;
				boolean success;
				try {
					elapsed = Long.parseLong(cols[headerIndex.get("elapsed")]);
				} catch (Exception e) { elapsed = 0; }
				try {
					timeStamp = Long.parseLong(cols[headerIndex.get("timeStamp")]);
				} catch (Exception e) { timeStamp = 0; }
				String respCodeStr = responseCode;
				int responseCodeInt;
				try {
					responseCodeInt = Integer.parseInt(respCodeStr.trim());
				} catch (Exception e) {
					responseCodeInt = -1; // fallback
				}
				success = "true".equalsIgnoreCase(cols[headerIndex.get("success")]);

				final String groupKey = threadGroup + "|" + samplerName;

				SampleRecord rec = new SampleRecord(elapsed, responseCodeInt, respCodeStr, timeStamp, success);

				grouped.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(rec);

				String codeKey = groupKey + "|" + responseCodeInt;
				errorCodeCount.put(codeKey, errorCodeCount.getOrDefault(codeKey, 0) + 1);
			}

			// Results will be made here
			List<PerfTestResult> perfTestResults = new ArrayList<>();
			List<PerfTestResultCode> perfTestResultCodes = new ArrayList<>();

			for (Map.Entry<String, List<SampleRecord>> groupEntry : grouped.entrySet()) {
				String groupKey = groupEntry.getKey();
				String[] parts = groupKey.split("\\|");
				String threadGroup = parts[0];
				String samplerName = parts[1];
				List<SampleRecord> samples = groupEntry.getValue();

				List<Long> latencies = new ArrayList<>();
				int total = samples.size();
				int errorCount = 0;

				int volumeLastMinute = 0;
				int volumeLast5Minutes = 0;
				long maxTimeStamp = samples.stream().mapToLong(r -> r.timeStamp).max().orElse(0);

				for (SampleRecord r : samples) {
					latencies.add(r.elapsed);
					if (!r.success) errorCount++;
				}

				long oneMinuteAgo = maxTimeStamp - 60_000;
				long fiveMinutesAgo = maxTimeStamp - 5 * 60_000;
				for (SampleRecord r : samples) {
					if (r.timeStamp >= oneMinuteAgo) volumeLastMinute++;
					if (r.timeStamp >= fiveMinutesAgo) volumeLast5Minutes++;
				}

				Collections.sort(latencies);
				int p50 = !latencies.isEmpty() ? calculatePercentile(latencies, 50) : 0;
				int p95 = !latencies.isEmpty() ? calculatePercentile(latencies, 95) : 0;
				int p99 = !latencies.isEmpty() ? calculatePercentile(latencies, 99) : 0;

				double errorRate = total > 0 ? (double)errorCount * 100.0 / total : 0.0;

				PerfTestResult res = new PerfTestResult();
				PerfTestResultId resId = new PerfTestResultId();
				// Needs a resultId (TestResult id), for demo will generate random
				resId.setResultId(testResult.getId());
				resId.setSamplerName(samplerName);
				resId.setThreadGroup(threadGroup);
				res.setId(resId);
				res.setTestResult(testResult);
				res.setSamplerName(samplerName);
				res.setThreadGroup(threadGroup);
				res.setP50LatencyMs(p50);
				res.setP95LatencyMs(p95);
				res.setP99LatencyMs(p99);
				res.setVolumeLastMinute(volumeLastMinute);
				res.setVolumeLast5Minutes(volumeLast5Minutes);
				res.setErrorRatePercent(errorRate);

				perfTestResults.add(res);

				// Now, PerfTestResultCode per error code
				Set<Integer> seenCodes = new HashSet<>();
				for (SampleRecord r : samples) seenCodes.add(r.responseCodeInt);

				for (int errorCode : seenCodes) {
					PerfTestResultCodeId codeId = new PerfTestResultCodeId();
					codeId.setResultId(testResult.getId());
					codeId.setErrorCode(errorCode);
					codeId.setSamplerName(samplerName);
					codeId.setThreadGroup(threadGroup);

					PerfTestResultCode prc = new PerfTestResultCode();
					prc.setId(codeId);
					String countKey = groupKey + "|" + errorCode;
					prc.setCount(errorCodeCount.getOrDefault(countKey, 0));
					perfTestResultCodes.add(prc);
				}
			}

			perfTestResultRepository.saveAll(perfTestResults);
			perfTestResultCodeRepository.saveAll(perfTestResultCodes);

			return true;
		} catch (IOException e) {
			throw new IOException("Failed to process JMeter results: " + e.getMessage(), e);
		}
	}
}

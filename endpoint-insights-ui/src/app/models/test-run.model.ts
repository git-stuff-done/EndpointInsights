export type TestRunStatus = 'PASS' | 'WARN' | 'FAIL' | 'UNKNOWN';

export interface PerfTestResultId {
  resultId: string;
  samplerName: string;
  threadGroup: string;
}

export interface PerfTestResult {
  errorRatePercent: number;
  id: PerfTestResultId;
  p50LatencyMs: number;
  p95LatencyMs: number;
  p99LatencyMs: number;
  latencyThresholdResult: string;
  latency_threshold: number;
  samplerName: string;
  threadGroup: string;
  volumeLast5Minutes: number;
  volumeLastMinute: number;
}

export interface TestResult {
  id: string;
  jobType: number;
  perfTestResult?: PerfTestResult;
}

export interface TestRun {
  runId: string;
  jobId: string | null;
  runBy: string;
  status: string;
  startedAt?: string;
  finishedAt?: string;
  batchId?: string;
  results: TestResult[];
}

export interface RecentActivity {
  runId: string;
  jobId?: string | null;
  batchId?: string | null;
  testName: string;
  group: string;
  dateRun: string;
  durationMs: number;
  startedBy: string;
  status: TestRunStatus;
  batchName?: string;
}

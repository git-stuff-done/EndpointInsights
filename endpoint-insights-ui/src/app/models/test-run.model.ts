export type TestRunStatus = 'PASS' | 'WARN' | 'FAIL' | 'UNKNOWN';

export interface TestRun {
  runId: string;
  jobId: string;
  runBy: string;
  status: TestRunStatus;
  startedAt?: string;
  finishedAt?: string;
}

export interface RecentActivity {
  runId: string;
  jobId?: string;
  testName: string;
  group: string;
  dateRun: string;
  durationMs: number;
  startedBy: string;
  status: TestRunStatus;
}

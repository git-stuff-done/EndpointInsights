export type TestRunStatus = 'PASS' | 'WARN' | 'FAIL' | 'UNKNOWN';

export interface TestRun {
  runId: string;
  jobId: string;
  runBy: string;
  status: TestRunStatus;
  startedAt?: string;
  finishedAt?: string;
}

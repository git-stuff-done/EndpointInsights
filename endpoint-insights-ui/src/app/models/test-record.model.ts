export type TestStatus = 'PASS' | 'WARN' | 'FAIL' | 'UNKNOWN';

export interface HttpBreakdown { code: number; count: number; }

export interface ThresholdConfig {
  latencyMs?: { warn: number; fail: number };
  errorRatePct?: { warn: number; fail: number };
  volumePerMin?: { warn: number; fail: number };
}

export interface TestRecord {
  id: string;
  name: string;
  description?: string;
  status: TestStatus;
  lastRunIso?: string;
  latencyMsP50?: number;
  latencyMsP95?: number;
  latencyMsP99?: number;
  volume1m?: number;
  volume5m?: number;
  httpBreakdown?: HttpBreakdown[];
  errorRatePct?: number;
  thresholds?: ThresholdConfig;
}

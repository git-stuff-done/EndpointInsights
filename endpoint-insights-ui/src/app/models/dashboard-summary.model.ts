import { DashboardTestActivity } from '../dashboard-component/dashboard-component';

export interface DashboardSummaryResponse {
  totalRuns: number;
  passedRuns: number;
  failedRuns: number;
  passRate: number;
  avgDurationMs: number;
  byStatus: Record<'PASS' | 'FAIL' | 'RUNNING' | 'PENDING', number>;
  recentActivity: DashboardTestActivity[];
}
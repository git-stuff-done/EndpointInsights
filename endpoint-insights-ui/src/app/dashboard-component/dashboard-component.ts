import {Component, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {MatButtonModule} from '@angular/material/button';
import {TestRunService} from '../services/test-run.service';
import {Router} from "@angular/router";
import {MatIcon} from "@angular/material/icon";

export interface DashboardTestActivity {
    id: string;
    testName: string;
    batchName: string | null;
    group: string;
    dateRun: Date;
    durationMs: number;
    startedBy: string;
    status: 'PASS' | 'FAIL' | 'RUNNING' | 'PENDING';
}

export interface DashboardAlert {
    id: string;
    message: string;
    severity: 'warning' | 'error';
}

@Component({
    selector: 'app-dashboard',
    standalone: true,
  imports: [CommonModule, MatButtonModule, MatIcon],
    templateUrl: './dashboard-component.html',
    styleUrls: ['./dashboard-component.scss'],
})
export class DashboardComponent implements OnInit {

    // KPI metrics
    activeJobsTotal = 12;
    activeJobsScheduled = 9;
    activeJobsManual = 3;

    failuresLast24h = 3;
    failureEndpointsCount = 2;

    passingPercentage = 92;
    passingRuns = 230;
    totalRuns = 250;

    averageLatencyMs = 350;
    latencyThresholdMs = 400;

    // Chart data placeholders
    apiPerformanceData = [];   // replace with real shape later
    apiTrendData = [];         // same here

    tests: DashboardTestActivity[] = [];

    constructor(private router: Router,
                private testRunService: TestRunService) { }

    ngOnInit(): void {
        this.testRunService.getRecentActivity(10).subscribe({
            next: (data) => {
                this.tests = data.map(r => ({
                    id: r.runId,
                    testName: r.testName,
                    batchName: r.batchName ?? null,
                    group: r.group,
                    dateRun: new Date(r.dateRun),
                    durationMs: r.durationMs,
                    startedBy: r.startedBy,
                    status: r.status as DashboardTestActivity['status'],
                }));
            },
            error: (err) => console.error('Failed to load recent activity', err)
        });
    }

    // Alerts mock data
    alerts: DashboardAlert[] = [
        {
            id: 'checkout-latency',
            message:
                'Checkout API latency spike, 600 ms average (threshold 400 ms), notified DevOps team',
            severity: 'warning'
        },
        {
            id: 'auth-error-rate',
            message:
                'Auth API error rate above 5 percent, job failed, email sent to API owners',
            severity: 'error'
        },
        {
            id: 'profile-missed',
            message:
                'Profile API scheduled run missed, retrying in 15 minutes',
            severity: 'warning'
        }
    ];

    // keep any other existing code you already had in here
    viewResult(id: string) {
        this.router.navigate(['/test-results/view'],  { state: { runId: id } });
    }
}

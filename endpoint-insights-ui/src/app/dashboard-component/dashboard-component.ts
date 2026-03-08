import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TestResultsCardComponent } from '../components/test-results-card/test-results-card.component';
import { TestRecord } from '../models/test-record.model';
import { MatButtonModule } from '@angular/material/button';
import { TestRunService } from '../services/test-run.service';
//import { ModalService } from '../shared/modal/modal.service';

export interface DashboardTestActivity {
    id: string;
    testName: string;
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
    imports: [CommonModule, MatButtonModule],
    templateUrl: './dashboard-component.html',
    styleUrls: ['./dashboard-component.scss'],
})
export class DashboardComponent implements OnInit {

    private testRunService = inject(TestRunService);

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

    ngOnInit(): void {
        this.testRunService.getRecentActivity(10).subscribe({
            next: (data) => {
                this.tests = data.map(r => ({
                    id: r.runId,
                    testName: r.testName,
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
}

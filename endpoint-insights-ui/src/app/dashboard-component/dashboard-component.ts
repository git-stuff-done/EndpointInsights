import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TestResultsCardComponent } from '../components/test-results-card/test-results-card.component';
import { TestRecord } from '../models/test-record.model';
import { MatButtonModule } from '@angular/material/button';
//import { ModalService } from '../shared/modal/modal.service';

export interface DashboardTestActivity {
    id: string;
    testName: string;
    group: string;
    dateRun: Date;
    durationMs: number;
    startedBy: string;
    status: 'PASS' | 'FAIL';
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
export class DashboardComponent {

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

    // Recent activity mock data
    tests: DashboardTestActivity[] = [
        {
            id: 'vision-api-daily',
            testName: 'Vision API',
            group: 'Daily',
            dateRun: new Date('2025-07-10'),
            durationMs: 230,
            startedBy: 'J. Brock',
            status: 'PASS'
        },
        {
            id: 'services-api-manual',
            testName: 'Services API',
            group: 'N/A',
            dateRun: new Date('2025-07-10'),
            durationMs: 20,
            startedBy: 'F. Zappa',
            status: 'PASS'
        }
    ];

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

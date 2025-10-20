import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TestCardComponent } from '../components/test-card/test-card.component';
import { TestRecord } from '../models/test-record.model';

@Component({
    selector: 'app-dashboard',
    standalone: true,
    imports: [ CommonModule, TestCardComponent,],
    templateUrl: './dashboard-component.html',
    styleUrls: ['./dashboard-component.scss'],
})
export class DashboardComponent {
    tests: TestRecord[] = [
        {
            id: 'login-api',
            name: 'Login API',
            description: 'Auth flow correctness & responsiveness.',
            status: 'PASS',
            lastRunIso: new Date().toISOString(),
            latencyMsP50: 42, latencyMsP95: 120, latencyMsP99: 210,
            volume1m: 530, volume5m: 2510,
            httpBreakdown: [{ code: 200, count: 2478 }, { code: 401, count: 8 }, { code: 500, count: 1 }],
            errorRatePct: 0.4,
            thresholds: { latencyMs: { warn: 200, fail: 400 }, errorRatePct: { warn: 1.0, fail: 2.5 }, volumePerMin: { warn: 100, fail: 20 } }
        },
        {
            id: 'payments',
            name: 'Payments',
            description: 'Card authorization and capture path.',
            status: 'FAIL',
            lastRunIso: new Date().toISOString(),
            latencyMsP50: 320, latencyMsP95: 900, latencyMsP99: 1900,
            volume1m: 75, volume5m: 450,
            httpBreakdown: [{ code: 200, count: 240 }, { code: 429, count: 22 }, { code: 500, count: 31 }],
            errorRatePct: 9.8,
            thresholds: { latencyMs: { warn: 250, fail: 600 }, errorRatePct: { warn: 2.0, fail: 5.0 }, volumePerMin: { warn: 60, fail: 30 } }
        }
    ];

    trackById = (_: number, t: TestRecord) => t.id;

}


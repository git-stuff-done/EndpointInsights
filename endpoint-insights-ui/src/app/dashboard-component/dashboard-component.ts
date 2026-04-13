import { Component, inject, OnInit, AfterViewInit, ViewChild, ElementRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TestRecord } from '../models/test-record.model';
import { MatButtonModule } from '@angular/material/button';
import { TestRunService } from '../services/test-run.service';
import {
  Chart,
  LineController,
  LineElement,
  PointElement,
  LinearScale,
  CategoryScale,
  Legend,
  Tooltip
} from 'chart.js';
import { PerformanceChartService } from '../services/performance-chart.service';
import { ChartPoint, ChartResponse } from '../models/chart.model';
import {Router} from "@angular/router";
import {MatIcon} from "@angular/material/icon";
import { DashboardSummaryService } from '../services/dashboard-summary.service';
import { DashboardSummaryResponse } from '../models/dashboard-summary.model';
import { JobService } from '../services/job-services';
import { Job } from '../models/job.model';

Chart.register(
  LineController,
  LineElement,
  PointElement,
  LinearScale,
  CategoryScale,
  Legend,
  Tooltip
);

export interface DashboardTestActivity {
    id: string;
    jobId?: string | null;
    batchId?: string | null;
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
export class DashboardComponent implements OnInit, AfterViewInit {
    @ViewChild('performanceCanvas')
    performanceCanvas!: ElementRef<HTMLCanvasElement>;

    private chart?: Chart;

    jobId = '';
    batchId = '';
    loading = false;
    error = '';

    chartResponse?: ChartResponse;

    lineChartLabels: string[] = [];
    lineChartData: { label: string; data: ChartPoint[] }[] = [];

    // Summary from backend
    summary?: DashboardSummaryResponse;

    // KPI getters — driven by summary
    get totalRuns()         { return this.summary?.totalRuns ?? 0; }
    get passingRuns()       { return this.summary?.passedRuns ?? 0; }
    get failuresLast24h()   { return this.summary?.failedRuns ?? 0; }
    get passingPercentage() { return this.summary ? Math.round(this.summary.passRate * 100) : 0; }
    get averageLatencyMs()  { return this.summary ? Math.round(this.summary.avgDurationMs) : 0; }
    get failureEndpointsCount() {
      if (!this.summary?.recentActivity) return 0;
      return new Set(
        this.summary.recentActivity
          .filter(t => t.status === 'FAIL')
          .map(t => t.testName)
      ).size;
    }
    private jobs: Job[] = [];
    get activeJobsTotal()     { return this.jobs.length; }

    // No backend source yet — keep static for now
//     activeJobsScheduled = 9;
//     activeJobsManual = 3;

    // Chart data placeholders
    apiPerformanceData = [];   // replace with real shape later
    apiTrendData = [];         // same here

    tests: DashboardTestActivity[] = [];

    constructor(private router: Router,
                private testRunService: TestRunService,
                private performanceChartService: PerformanceChartService,
                private dashboardSummaryService: DashboardSummaryService,
                private jobService: JobService
                ) { }

    ngOnInit(): void {
        this.testRunService.getRecentActivity(10).subscribe({
            next: (data) => {
                this.tests = data.map(r => ({
                    id: r.runId,
                    jobId: r.jobId,
                    batchId: r.batchId,
                    testName: r.testName,
                    batchName: r.batchName ?? null,
                    group: r.group,
                    dateRun: new Date(r.dateRun),
                    durationMs: r.durationMs,
                    startedBy: r.startedBy,
                    status: r.status as DashboardTestActivity['status'],
                }));

            // POST the same activity data to /dashboard/summary
        this.dashboardSummaryService.loadDashboardSummary(100).subscribe({
            next: (summary) => this.summary = summary,
            error: (err) => console.error('Failed to load summary', err)
        });

        this.jobService.getAllJobs().subscribe({
            next: (jobs) => this.jobs = jobs,
            error: (err) => console.error('Failed to load jobs', err)
        });

                const mostRecentPassingRun = data.find(
                  r => (r.status === 'PASS' || r.status === 'FAIL') && (r.jobId || r.batchId)
                );

                if (mostRecentPassingRun) {
                  this.jobId = mostRecentPassingRun.jobId ?? '';
                  this.batchId = mostRecentPassingRun.batchId ?? '';
                  this.loadChart();
                } else {
                  this.error = 'No recent passing test found.';
                }
            },
        error: (err) => {
            console.error('Failed to load recent activity', err);
                this.error = 'Failed to load recent activity';
            }
        });
    }

    ngAfterViewInit(): void {
//         this.loadChart();
      }

    loadChart(): void {
        this.loading = true;
        this.error = '';

        this.performanceChartService.getApiPerformanceChart(
          this.jobId || undefined,
          this.batchId || undefined
        ).subscribe({
          next: (response) => {
            this.chartResponse = response;
            this.mapToChart(response);
            this.loading = false;
            setTimeout(() => {
                    this.renderChart();
                  });
          },
          error: (err) => {
            console.error('Failed to load chart', err);
            this.error = 'Failed to load chart';
            this.loading = false;
          }
        });
      }

    private mapToChart(response: ChartResponse): void {
        if (!response.series?.length) {
          this.lineChartLabels = [];
          this.lineChartData = [];
          return;
        }

        this.lineChartLabels = response.series[0].data.map((p: ChartPoint) => p.label);
        this.lineChartData = response.series.map(series => ({
          label: series.name,
          data: series.data
        }));
      }

    private renderChart(): void {
      if (!this.performanceCanvas) return;

      if (this.chart) {
        this.chart.destroy();
      }

      this.chart = new Chart(this.performanceCanvas.nativeElement, {
        type: 'line',
        data: {
          labels: this.lineChartLabels,
          datasets: this.chartResponse!.series.map(series => ({
            label: series.name,
            data: series.data.map(p => p.value),

            borderColor: '#3b82f6',
            backgroundColor: 'rgba(59, 130, 246, 0.1)',
            tension: 0.4,
            fill: true,

            pointRadius: 4,
            pointHoverRadius: 6,

            pointBackgroundColor: series.data.map(p =>
              p.status === 'PASS' ? '#22c55e' : '#ef4444'
            ),

            pointBorderColor: series.data.map(p =>
              p.status === 'PASS' ? '#16a34a' : '#dc2626'
            ),
          }))
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,

          plugins: {
            legend: {
              display: true,
              position: 'top',
              labels: {
                color: '#374151',
                font: {
                  size: 12
                }
              }
            }
          },

          scales: {
            x: {
              grid: {
                display: true
              },
              ticks: {
                color: '#6b7280'
              }
            },
            y: {
              grid: {
                color: 'rgba(0,0,0,0.05)'
              },
              ticks: {
                color: '#6b7280'
              }
            }
          }
        }
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

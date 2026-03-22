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
  imports: [CommonModule, MatButtonModule, MatIcon],
    templateUrl: './dashboard-component.html',
    styleUrls: ['./dashboard-component.scss'],
})
export class DashboardComponent implements OnInit, AfterViewInit {
    @ViewChild('performanceCanvas')
    performanceCanvas!: ElementRef<HTMLCanvasElement>;

    private chart?: Chart;

    jobId = '';
    loading = false;
    error = '';

    chartResponse?: ChartResponse;

    lineChartLabels: string[] = [];
    lineChartData: { label: string; data: number[] }[] = [];

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
                private testRunService: TestRunService,
                private performanceChartService: PerformanceChartService
                ) { }

    ngOnInit(): void {
        this.testRunService.getRecentActivity(10).subscribe({
            next: (data) => {
                this.tests = data.map(r => ({
                    id: r.runId,
                    jobId: r.jobId,
                    testName: r.testName,
                    group: r.group,
                    dateRun: new Date(r.dateRun),
                    durationMs: r.durationMs,
                    startedBy: r.startedBy,
                    status: r.status as DashboardTestActivity['status'],
                }));
                const mostRecentPassingRun = data.find(
                        r => r.status === 'PASS' && r.jobId
                      );

                      if (mostRecentPassingRun && mostRecentPassingRun.jobId) {
                        this.jobId = mostRecentPassingRun.jobId;
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

        this.performanceChartService.getApiPerformanceChart(this.jobId).subscribe({
          next: (response) => {
            this.chartResponse = response;
            this.mapToChart(response);
            this.renderChart();
            this.loading = false;
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
          data: series.data.map((p: ChartPoint) => p.value)
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
          datasets: this.lineChartData.map(series => ({
            label: series.label,
            data: series.data,

            borderColor: '#3b82f6', // blue
            backgroundColor: 'rgba(59, 130, 246, 0.1)',

            tension: 0.4, // smooth curve
            pointRadius: 4,
            pointHoverRadius: 6,
            fill: true
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

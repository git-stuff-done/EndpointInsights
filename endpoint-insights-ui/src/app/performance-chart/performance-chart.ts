import {Component, ElementRef, Input, SimpleChanges, ViewChild} from '@angular/core';
import {ChartPoint, ChartResponse} from "../models/chart.model";
import {Chart} from "chart.js";
import {PerformanceChartService} from "../services/performance-chart.service";

@Component({
  selector: 'app-performance-chart',
  imports: [],
  templateUrl: './performance-chart.html',
  styleUrl: './performance-chart.scss',
})
export class PerformanceChart {


  chartResponse?: ChartResponse;

  lineChartLabels: string[] = [];
  lineChartData: { label: string; data: ChartPoint[] }[] = [];

  @ViewChild('performanceCanvas')
  performanceCanvas!: ElementRef<HTMLCanvasElement>;

  private chart?: Chart;
  loading = false;

  error: string = '';

  @Input() jobId?: string;
  @Input() batchId?: string;

  constructor(private performanceChartService: PerformanceChartService) {}

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['jobId'] || changes['batchId']) {
      this.loadChart();
    }
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

}

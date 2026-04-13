import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {TestRunService} from "../services/test-run.service";
import {ActivatedRoute, Router} from "@angular/router";
import {PerfTestResult, PerfTestResultId, TestResult, TestRun} from "../models/test-run.model";
import {
  MatCell, MatCellDef, MatColumnDef,
  MatHeaderCell, MatHeaderCellDef,
  MatHeaderRow, MatHeaderRowDef,
  MatRow,
  MatRowDef,
  MatTable,
  MatTableDataSource,
} from "@angular/material/table";
import {MatIcon} from "@angular/material/icon";
import {DatePipe, DecimalPipe, NgClass, SlicePipe} from "@angular/common";
import {MatPaginator} from "@angular/material/paginator";
import {NotificationService} from "../services/notification.service";

@Component({
  selector: 'app-view-result',
  imports: [
    MatTable,
    MatHeaderCell,
    MatCell,
    MatHeaderRow,
    MatRow,
    MatRowDef,
    MatHeaderRowDef,
    MatCellDef,
    MatHeaderCellDef,
    DecimalPipe,
    MatColumnDef,
    NgClass,
    DatePipe,
    SlicePipe,
    MatPaginator,
    MatIcon
  ],
  templateUrl: './view-result.html',
  styleUrl: './view-result.scss',
  standalone: true,
})
export class ViewResult implements OnInit, AfterViewInit {

  public testRun?: TestRun;
  public testResultsDataSource: MatTableDataSource<PerfTestResult> = new MatTableDataSource<PerfTestResult>();
  public displayedColumns: string[] = ['threadGroup', 'samplerName', 'p50LatencyMs', 'p95LatencyMs', 'p99LatencyMs', 'latencyThreshold', 'errorRatePercent', 'volumeLastMinute', 'volumeLast5Minutes']

  @ViewChild(MatPaginator, {static: false}) paginator!: MatPaginator;

  get duration(): string {
    if (!this.testRun) return '';

    const finish: string = this.testRun.finishedAt ?? '';
    const start: string = this.testRun.startedAt ?? '';

    const ms = new Date(finish).getTime() - new Date(start).getTime();
    return (ms / 1000).toFixed(1);
  }

  constructor(private testRunService: TestRunService,
              private activatedRoute: ActivatedRoute,
              private router: Router,
              private notificationService: NotificationService) {
  }

  ngOnInit() {
    const state = window.history.state;

    // First try to load from state, otherwise from query param
    if (state?.runId) {
      this.getTestRun(state.runId);
    } else {
      this.activatedRoute.queryParams.subscribe(params => {
        const id = params['id'];
        if (id) {
          this.getTestRun(id);
        }
      });
    }
  }

  ngAfterViewInit() {
    this.testResultsDataSource.paginator = this.paginator;
  }

  private getTestRun(id: string) {
    this.testRunService.getRun(id).subscribe(run => {
      this.testRun = run;
      this.testResultsDataSource.data = run.results.map(result => result.perfTestResult as PerfTestResult);
      this.testResultsDataSource.paginator = this.paginator;
    })
  }

  public getErrorRateClass(errorRate: number): string {
    if (errorRate > 0.5) {
      return 'error-rate-high';
    } else if (errorRate > 0.1) {
      return 'error-rate-medium';
    } else {
      return 'error-rate-low';
    }
  }

    public getLatencyThresholdClass(results: string): string {
        if (results === 'FAIL') {
            return 'error-rate-high';
        } else if (results === 'WARN') {
            return 'error-rate-medium';
        } else {
            return 'error-rate-low';
        }
    }

    public delete() {
      if (!this.testRun) {
        console.error('No test run available to delete');
        return;
      }
        this.testRunService.deleteRun(this.testRun.runId).subscribe({
          next: (res) => {
            this.router.navigate(['/test-results']).then(() => {
              this.notificationService.showToast('Test run deleted successfully', 'success');
            });
          },
          error: (err) => {
            console.error('Error deleting test run:', err);
            this.notificationService.showToast('Failed to delete test run', 'error');
          }
        });
    }

}

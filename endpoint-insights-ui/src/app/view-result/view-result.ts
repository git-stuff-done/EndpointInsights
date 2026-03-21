import {Component, OnInit} from '@angular/core';
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
  MatTableDataSource
} from "@angular/material/table";
import {DatePipe, DecimalPipe, NgClass, SlicePipe} from "@angular/common";

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
    SlicePipe
  ],
  templateUrl: './view-result.html',
  styleUrl: './view-result.scss',
})
export class ViewResult implements OnInit {

  public testRun?: TestRun;
  public testResultsDataSource: MatTableDataSource<PerfTestResult> = new MatTableDataSource<PerfTestResult>();
  public displayedColumns: string[] = ['threadGroup', 'samplerName', 'p50LatencyMs', 'p95LatencyMs', 'p99LatencyMs', 'errorRatePercent', 'volumeLastMinute', 'volumeLast5Minutes']

  get duration(): string {
    if (!this.testRun) return '';

    const finish: string = this.testRun.finishedAt ?? '';
    const start: string = this.testRun.startedAt ?? '';

    const ms = new Date(finish).getTime() - new Date(start).getTime();
    return (ms / 1000).toFixed(1);
  }

  constructor(private testRunService: TestRunService,
              private router: Router,
              private activatedRoute: ActivatedRoute) {
  }

  ngOnInit() {
    const state = window.history.state;

    // First try to load from state, otherwise from query param
    if (state.runId) {
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

  private getTestRun(id: string) {
    this.testRunService.getRun(id).subscribe(run => {
      this.testRun = run;
      this.testResultsDataSource.data = run.results.map(result => result.perfTestResult as PerfTestResult);
      console.log(this.testResultsDataSource.data.length);
    })
  }

}

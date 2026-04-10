import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { of } from 'rxjs';
import { DashboardComponent } from './dashboard-component';
import { TestRunService } from '../services/test-run.service';
import { PerformanceChartService } from '../services/performance-chart.service';
import { DashboardSummaryService } from '../services/dashboard-summary.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { JobService } from '../services/job.service';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;

  const mockTestRunService = {
    getRecentActivity: jasmine.createSpy('getRecentActivity').and.returnValue(
      of([
        {
          runId: 'run-1',
          jobId: 'job-1',
          batchId: null,
          testName: 'Endpoint_Insight_Health',
          group: 'Daily',
          dateRun: '2026-03-22T10:00:00Z',
          durationMs: 6469,
          startedBy: 'tester',
          status: 'PASS'
        }
      ])
    )
  };

  const mockJobService = {
    getAllJobs: jasmine.createSpy('getAllJobs').and.returnValue(of([
      { jobId: '1', name: 'Job A', type: 'SCHEDULED' },
      { jobId: '2', name: 'Job B', type: 'MANUAL' },
    ]))
  };

  const mockPerformanceChartService = {
    getApiPerformanceChart: jasmine.createSpy('getApiPerformanceChart').and.returnValue(
      of({
        title: 'Endpoint_Insight_Health API Performance',
        xAxis: 'runNumber',
        series: [
          {
            name: 'Run Duration (ms)',
            data: [
              { label: '1', value: 6469, status: 'PASS' },
              { label: '2', value: 6254, status: 'FAIL' }
            ]
          }
        ]
      })
    )
  };

    const mockDashboardSummaryService = {
        getSummary: jasmine.createSpy('getSummary').and.returnValue(
        of({
            totalRuns: 10,
            passedRuns: 8,
            failedRuns: 2,
            passRate: 0.8,
            avgDurationMs: 6469,
            byStatus: { PASS: 8, FAIL: 2, RUNNING: 0, PENDING: 0 },
            recentActivity: []
        })
        ),
        loadDashboardSummary: jasmine.createSpy('loadDashboardSummary').and.returnValue(
        of({
            totalRuns: 10,
            passedRuns: 8,
            failedRuns: 2,
            passRate: 0.8,
            avgDurationMs: 6469,
            byStatus: { PASS: 8, FAIL: 2, RUNNING: 0, PENDING: 0 },
            recentActivity: []
        })
        )
    };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardComponent, HttpClientTestingModule],
      providers: [
        { provide: TestRunService, useValue: mockTestRunService },
        { provide: PerformanceChartService, useValue: mockPerformanceChartService },
        { provide: DashboardSummaryService, useValue: mockDashboardSummaryService },
        { provide: JobService, useValue: mockJobService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;

    spyOn<any>(component, 'renderChart').and.stub();
  });

  afterEach(() => {
    mockTestRunService.getRecentActivity.calls.reset();
    mockPerformanceChartService.getApiPerformanceChart.calls.reset();
    mockDashboardSummaryService.getSummary.calls.reset();
    mockDashboardSummaryService.loadDashboardSummary.calls.reset();
    mockJobService.getAllJobs.calls.reset();
  });

  it('should create', () => {
    fixture.detectChanges();
    expect(component).toBeTruthy();
  });

  it('loads recent activity on init', () => {
    fixture.detectChanges();

    expect(mockTestRunService.getRecentActivity).toHaveBeenCalledWith(10);
    expect(component.tests.length).toBe(1);
    expect(component.tests[0].testName).toBe('Endpoint_Insight_Health');
    expect(component.tests[0].status).toBe('PASS');
  });

  it('loads chart for the most recent pass/fail run', () => {
    fixture.detectChanges();

    expect(mockPerformanceChartService.getApiPerformanceChart).toHaveBeenCalledWith('job-1', undefined);
  });

  it('maps chart response into labels and chart point data', () => {
    fixture.detectChanges();

    expect(component.lineChartLabels).toEqual(['1', '2']);
    expect(component.lineChartData).toEqual([
      {
        label: 'Run Duration (ms)',
        data: [
          { label: '1', value: 6469, status: 'PASS' },
          { label: '2', value: 6254, status: 'FAIL' }
        ]
      }
    ]);
  });

  it('calls renderChart after chart data loads', fakeAsync(() => {
    fixture.detectChanges();
    tick();

    expect((component as any).renderChart).toHaveBeenCalled();
  }));

  it('sets jobId from the most recent matching run', () => {
    fixture.detectChanges();

    expect(component.jobId).toBe('job-1');
    expect(component.batchId).toBe('');
  });

  it('calls loadDashboardSummary on init', () => {
    fixture.detectChanges();
    expect(mockDashboardSummaryService.loadDashboardSummary).toHaveBeenCalled();
  });

  it('populates summary KPIs from the summary response', () => {
    fixture.detectChanges();

    expect(component.totalRuns).toBe(10);
    expect(component.passingRuns).toBe(8);
    expect(component.failuresLast24h).toBe(2);
    expect(component.passingPercentage).toBe(80);
  });

  it('calculates averageLatencyMs from summary', () => {
    fixture.detectChanges();
    expect(component.averageLatencyMs).toBe(6469);
  });

  it('returns 0 for failureEndpointsCount when summary has no recentActivity', () => {
    fixture.detectChanges();
    expect(component.failureEndpointsCount).toBe(0);
  });

  it('counts distinct failing endpoints from recentActivity', () => {
    component.summary = {
        totalRuns: 2,
        passedRuns: 0,
        failedRuns: 2,
        passRate: 0,
        avgDurationMs: 100,
        byStatus: { PASS: 0, FAIL: 2, RUNNING: 0, PENDING: 0 },
        recentActivity: [
        { testName: 'API-A', status: 'FAIL' } as any,
        { testName: 'API-A', status: 'FAIL' } as any,  // same endpoint, should count once
        { testName: 'API-B', status: 'FAIL' } as any,
        ]
    };
    expect(component.failureEndpointsCount).toBe(2);
  });

  it('calls loadDashboardSummary with limit 100 on init', () => {
    fixture.detectChanges();
    expect(mockDashboardSummaryService.loadDashboardSummary).toHaveBeenCalledWith(100);
  });

  it('sets summary from loadDashboardSummary response', () => {
    fixture.detectChanges();
    expect(component.summary).toBeTruthy();
    expect(component.summary?.totalRuns).toBe(10);
  });

  it('returns 0 for activeJobsTotal when no jobs loaded', () => {
    expect(component.activeJobsTotal).toBe(0);
  });

  it('calculates activeJobsScheduled from jobs with SCHEDULED type', () => {
    (component as any).jobs = [
      { jobId: '1', name: 'Job A', type: 'SCHEDULED' },
      { jobId: '2', name: 'Job B', type: 'MANUAL' },
      { jobId: '3', name: 'Job C', type: 'SCHEDULED' },
    ];
    expect(component.activeJobsScheduled).toBe(2);
    expect(component.activeJobsManual).toBe(1);
    expect(component.activeJobsTotal).toBe(3);
  });

  it('returns 0 for passingPercentage when summary is undefined', () => {
    component.summary = undefined;
    expect(component.passingPercentage).toBe(0);
    expect(component.averageLatencyMs).toBe(0);
  });

  it('returns 0 for all KPIs when summary is undefined', () => {
    component.summary = undefined;
    expect(component.totalRuns).toBe(0);
    expect(component.passingRuns).toBe(0);
    expect(component.failuresLast24h).toBe(0);
    expect(component.failureEndpointsCount).toBe(0);
  });
});

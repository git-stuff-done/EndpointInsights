import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { DashboardSummaryService } from './dashboard-summary.service';
import { TestRunService } from './test-run.service';
import { DashboardSummaryResponse } from '../models/dashboard-summary.model';
import { RecentActivity } from '../models/test-run.model';
import { AuthenticationService } from './authentication.service';
import { environment } from '../../environment';

const BASE = `${environment.apiUrl}/dashboard`;

const mockAuthService = {
  authState$: of(null),
  getToken: () => null,
};

const mockActivity: RecentActivity[] = [
  {
    runId: 'run-1',
    jobId: 'job-1',
    batchId: null,
    testName: 'API Health Check',
    batchName: null,
    group: 'Daily',
    dateRun: '2026-03-22T10:00:00Z',
    durationMs: 6469,
    startedBy: 'tester',
    status: 'PASS'
  } as any
];

const mockSummaryResponse: DashboardSummaryResponse = {
  totalRuns: 10,
  passedRuns: 8,
  failedRuns: 2,
  passRate: 0.8,
  avgDurationMs: 6469,
  byStatus: { PASS: 8, FAIL: 2, RUNNING: 0, PENDING: 0 },
  recentActivity: []
};

describe('DashboardSummaryService', () => {
  let service: DashboardSummaryService;
  let httpMock: HttpTestingController;

  const mockTestRunService = {
    getRecentActivity: jasmine.createSpy('getRecentActivity').and.returnValue(
      of(mockActivity)
    )
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        DashboardSummaryService,
        { provide: AuthenticationService, useValue: mockAuthService },
        { provide: TestRunService, useValue: mockTestRunService }
      ]
    });

    service = TestBed.inject(DashboardSummaryService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    mockTestRunService.getRecentActivity.calls.reset();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('getSummary should POST activity to /dashboard/summary', () => {
    service.getSummary(mockActivity).subscribe(result => {
      expect(result).toEqual(mockSummaryResponse);
    });

    const req = httpMock.expectOne(`${BASE}/summary`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(mockActivity);
    req.flush(mockSummaryResponse);
  });

  it('loadDashboardSummary should fetch activity then POST to summary', () => {
    service.loadDashboardSummary(50).subscribe(result => {
      expect(result).toEqual(mockSummaryResponse);
    });

    expect(mockTestRunService.getRecentActivity).toHaveBeenCalledWith(50);

    const req = httpMock.expectOne(`${BASE}/summary`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(mockActivity);
    req.flush(mockSummaryResponse);
  });

  it('loadDashboardSummary should use default limit of 10', () => {
    service.loadDashboardSummary().subscribe();

    expect(mockTestRunService.getRecentActivity).toHaveBeenCalledWith(10);

    const req = httpMock.expectOne(`${BASE}/summary`);
    req.flush(mockSummaryResponse);
  });
});
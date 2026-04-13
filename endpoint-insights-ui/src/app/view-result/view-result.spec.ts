import {ComponentFixture, TestBed} from '@angular/core/testing';

import {ViewResult} from './view-result';
import {provideHttpClient} from "@angular/common/http";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {Params, provideRouter, Router} from "@angular/router";
import {of, throwError} from "rxjs";
import {TestRun} from "../models/test-run.model";
import {NotificationService} from "../services/notification.service";

describe('ViewResult', () => {
  let component: ViewResult;
  let fixture: ComponentFixture<ViewResult>;

  const mockTestRun: TestRun = {
    batchId: 'efe636c1-4a02-47c8-b1d3-36d9bde4224c',
    finishedAt: '2026-03-21T19:00:06.440538Z',
    jobId: null,
    runBy: 'system',
    runId: 'eda90106-635f-44c0-acff-b45618a91433',
    startedAt: '2026-03-21T19:00:00.364238Z',
    status: 'COMPLETED',
    results: [
      {
        id: '56e4a45d-cc74-40ca-8761-ed3c8888e919',
        jobType: 0,
        perfTestResult: {
          errorRatePercent: 0.0,
          id: {
            resultId: '56e4a45d-cc74-40ca-8761-ed3c8888e919',
            samplerName: 'GET /api/health',
            threadGroup: '100 User Load 1'
          },
          p50LatencyMs: 2,
          p95LatencyMs: 26,
          p99LatencyMs: 26,
          samplerName: 'GET /api/health',
          threadGroup: '100 User Load 1',
          volumeLast5Minutes: 100,
          volumeLastMinute: 100,
        latencyThresholdResult: 'PASS',
        latency_threshold: 500,
        }
      }
    ]
  };


  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ViewResult],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([])
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ViewResult);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load test run id from state', () => {
    const getTestRunSpy = spyOn(component['testRunService'], 'getRun').and.returnValue(of(mockTestRun));

    window.history.replaceState({runId: 'eda90106-635f-44c0-acff-b45618a91433'}, '');
    component.ngOnInit();

    expect(getTestRunSpy).toHaveBeenCalledOnceWith('eda90106-635f-44c0-acff-b45618a91433');
  });


  it('should load test run from query params if state not present', () => {
    const getTestRunSpy = spyOn(component['testRunService'], 'getRun').and.returnValue(of(mockTestRun));

    window.history.replaceState({}, '');

    component['activatedRoute'].queryParams = of({id: '123'} as Params);

    component.ngOnInit();

    expect(getTestRunSpy).toHaveBeenCalledOnceWith('123');
  });

  it('should should not try to get the test run when no id is present', () => {
    const getTestRunSpy = spyOn(component['testRunService'], 'getRun').and.returnValue(of(mockTestRun));

    window.history.replaceState({}, '');

    component['activatedRoute'].queryParams = of({} as Params);

    component.ngOnInit();

    expect(getTestRunSpy).not.toHaveBeenCalled();
  });

  describe('delete()', () => {
    it('should do nothing when testRun is not set', () => {
      const deleteRunSpy = spyOn(component['testRunService'], 'deleteRun');
      component.testRun = undefined;

      component.delete();

      expect(deleteRunSpy).not.toHaveBeenCalled();
    });

    it('should call deleteRun, navigate to test-results, and show success toast on success', async () => {
      const router = TestBed.inject(Router);
      const notificationService = TestBed.inject(NotificationService);
      spyOn(component['testRunService'], 'deleteRun').and.returnValue(of({}));
      const navigateSpy = spyOn(router, 'navigate').and.returnValue(Promise.resolve(true));
      const toastSpy = spyOn(notificationService, 'showToast');
      component.testRun = mockTestRun;

      component.delete();

      expect(component['testRunService'].deleteRun).toHaveBeenCalledOnceWith(mockTestRun.runId);
      await fixture.whenStable();
      expect(navigateSpy).toHaveBeenCalledOnceWith(['/test-results']);
      expect(toastSpy).toHaveBeenCalledOnceWith('Test run deleted successfully', 'success');
    });

    it('should log an error when deleteRun fails', () => {
      const error = new Error('server error');
      spyOn(component['testRunService'], 'deleteRun').and.returnValue(throwError(() => error));
      const consoleSpy = spyOn(console, 'error');
      component.testRun = mockTestRun;

      component.delete();

      expect(consoleSpy).toHaveBeenCalledWith('Error deleting test run:', error);
    });
  });

  it('should return correct error rate class for different error rates', () => {
    // Test for low error rate (<= 0.1)
    expect(component.getErrorRateClass(0)).toBe('error-rate-low');
    expect(component.getErrorRateClass(0.05)).toBe('error-rate-low');
    expect(component.getErrorRateClass(0.1)).toBe('error-rate-low');

    // Test for medium error rate (> 0.1, <= 0.5)
    expect(component.getErrorRateClass(0.11)).toBe('error-rate-medium');
    expect(component.getErrorRateClass(0.3)).toBe('error-rate-medium');
    expect(component.getErrorRateClass(0.5)).toBe('error-rate-medium');

    // Test for high error rate (> 0.5)
    expect(component.getErrorRateClass(0.51)).toBe('error-rate-high');
    expect(component.getErrorRateClass(1)).toBe('error-rate-high');
  });


});

import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { PerformanceChartService } from './performance-chart.service';
import { HttpInterceptorService } from './http-interceptor.service';
import { AuthenticationService } from './authentication.service';
import { of } from 'rxjs';

describe('PerformanceChartService', () => {
  let service: PerformanceChartService;
  let httpMock: HttpTestingController;

  // mock auth service since interceptor depends on it
  const mockAuthService = {
    authState$: of(null),
    getToken: () => null
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        PerformanceChartService,
        HttpInterceptorService,
        { provide: AuthenticationService, useValue: mockAuthService }
      ]
    });

    service = TestBed.inject(PerformanceChartService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('requests API performance chart with jobId and limit', () => {
    const jobId = '123';
    const limit = 5;

    service.getApiPerformanceChart(jobId, limit).subscribe();

    const req = httpMock.expectOne(
      `http://localhost:8080/api/dashboard/charts/performance/${jobId}`
    );

    expect(req.request.method).toBe('GET');

    req.flush({
      title: '',
      xAxis: '',
      series: []
    });
  });

  it('maps response body correctly', () => {
    const jobId = '123';

    const mockResponse = {
      title: 'Test Chart',
      xAxis: 'runNumber',
      series: [
        {
          name: 'Run Duration (ms)',
          data: [
            { label: '1', value: 100 },
            { label: '2', value: 200 }
          ]
        }
      ]
    };

    let result: any;

    service.getApiPerformanceChart(jobId, 5).subscribe(res => {
      result = res;
    });

    const req = httpMock.expectOne(
      `http://localhost:8080/api/dashboard/charts/performance/${jobId}`
    );

    req.flush(mockResponse);

    expect(result).toEqual(mockResponse);
    expect(result.series.length).toBe(1);
    expect(result.series[0].data[0].value).toBe(100);
  });
});
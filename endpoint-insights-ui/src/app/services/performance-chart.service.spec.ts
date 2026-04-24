import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { PerformanceChartService } from './performance-chart.service';
import { HttpInterceptorService } from './http-interceptor.service';
import { AuthenticationService } from './authentication.service';
import { of } from 'rxjs';

describe('PerformanceChartService', () => {
  let service: PerformanceChartService;
  let httpMock: HttpTestingController;

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

    service.getApiPerformanceChart(jobId, undefined, limit).subscribe();

    const req = httpMock.expectOne(
      `https://d2wravsw1nwfu2.cloudfront.net/api/dashboard/charts/performance?limit=5&jobId=123`
    );

    expect(req.request.method).toBe('GET');

    req.flush({
      body: {
        title: '',
        xAxis: '',
        series: []
      }
    });
  });

  it('requests API performance chart with batchId and limit', () => {
    const batchId = 'batch-456';
    const limit = 7;

    service.getApiPerformanceChart(undefined, batchId, limit).subscribe();

    const req = httpMock.expectOne(
      `https://d2wravsw1nwfu2.cloudfront.net/api/dashboard/charts/performance?limit=7&batchId=batch-456`
    );

    expect(req.request.method).toBe('GET');

    req.flush({
      body: {
        title: '',
        xAxis: '',
        series: []
      }
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
            { label: '1', value: 100, status: 'PASS' },
            { label: '2', value: 200, status: 'FAIL' }
          ]
        }
      ]
    };

    let result: any;

    service.getApiPerformanceChart(jobId, undefined, 5).subscribe(res => {
      result = res;
    });

    const req = httpMock.expectOne(
      `https://d2wravsw1nwfu2.cloudfront.net/api/dashboard/charts/performance?limit=5&jobId=123`
    );

    req.flush(mockResponse);

    expect(result).toEqual(mockResponse);
    expect(result.series.length).toBe(1);
    expect(result.series[0].data[0].value).toBe(100);
    expect(result.series[0].data[0].status).toBe('PASS');
  });

  it('returns fallback response when body is missing', () => {
    let result: any;

    service.getApiPerformanceChart('123').subscribe(res => {
      result = res;
    });

    const req = httpMock.expectOne(
      `https://d2wravsw1nwfu2.cloudfront.net/api/dashboard/charts/performance?limit=10&jobId=123`
    );

    req.flush(null);

    expect(result).toEqual({
      title: '',
      xAxis: '',
      series: []
    });
  });

  it('throws an error if both jobId and batchId are provided', () => {
    expect(() => {
      service.getApiPerformanceChart('123', '456', 5);
    }).toThrowError('Provide only one of jobId or batchId');
  });
});
import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';

import {PerformanceChart} from './performance-chart';
import {of, throwError} from "rxjs";
import {PerformanceChartService} from "../services/performance-chart.service";
import {SimpleChange} from "@angular/core";
import {ChartResponse} from "../models/chart.model";

describe('PerformanceChart', () => {
  let component: PerformanceChart;
  let fixture: ComponentFixture<PerformanceChart>;

  const mockChartResponse: ChartResponse = {
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
  };

  const mockPerformanceChartService = {
    getApiPerformanceChart: jasmine.createSpy('getApiPerformanceChart').and.returnValue(
        of(mockChartResponse)
    )
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PerformanceChart],
      providers: [
        { provide: PerformanceChartService, useValue: mockPerformanceChartService }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PerformanceChart);
    component = fixture.componentInstance;
    fixture.detectChanges();

    spyOn<any>(component, 'renderChart').and.stub();
  });

  afterEach(() => {
    mockPerformanceChartService.getApiPerformanceChart.calls.reset();
    mockPerformanceChartService.getApiPerformanceChart.and.returnValue(of(mockChartResponse));
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnChanges', () => {
    it('should call loadChart when jobId changes', () => {
      spyOn(component, 'loadChart');
      component.ngOnChanges({ jobId: new SimpleChange(null, 'job-1', false) });
      expect(component.loadChart).toHaveBeenCalled();
    });

    it('should call loadChart when batchId changes', () => {
      spyOn(component, 'loadChart');
      component.ngOnChanges({ batchId: new SimpleChange(null, 'batch-1', false) });
      expect(component.loadChart).toHaveBeenCalled();
    });

    it('should not call loadChart when unrelated properties change', () => {
      spyOn(component, 'loadChart');
      component.ngOnChanges({ someOtherProp: new SimpleChange(null, 'value', false) });
      expect(component.loadChart).not.toHaveBeenCalled();
    });
  });

  describe('loadChart', () => {
    it('should set loading to true and clear error before calling the service', () => {
      component.error = 'previous error';
      component.jobId = 'job-1';
      let loadingDuringCall = false;
      let errorDuringCall = 'not-cleared';
      mockPerformanceChartService.getApiPerformanceChart.and.callFake(() => {
        loadingDuringCall = component.loading;
        errorDuringCall = component.error;
        return of(mockChartResponse);
      });

      component.loadChart();

      expect(loadingDuringCall).toBeTrue();
      expect(errorDuringCall).toBe('');
    });

    it('should not call the service when jobId and batchId are both undefined', () => {
      component.jobId = undefined;
      component.batchId = undefined;
      component.loadChart();
      expect(mockPerformanceChartService.getApiPerformanceChart).not.toHaveBeenCalled();
    });

    it('should leave loading as true when returning early with no ids', () => {
      component.jobId = undefined;
      component.batchId = undefined;
      component.loadChart();
      expect(component.loading).toBeTrue();
    });

    it('should call the service with jobId when only jobId is set', () => {
      component.jobId = 'job-1';
      component.batchId = undefined;
      component.loadChart();
      expect(mockPerformanceChartService.getApiPerformanceChart).toHaveBeenCalledWith('job-1', undefined);
    });

    it('should call the service with batchId when only batchId is set', () => {
      component.jobId = undefined;
      component.batchId = 'batch-1';
      component.loadChart();
      expect(mockPerformanceChartService.getApiPerformanceChart).toHaveBeenCalledWith(undefined, 'batch-1');
    });

    it('should set chartResponse and loading to false on success', () => {
      component.jobId = 'job-1';
      component.loadChart();
      expect(component.chartResponse).toEqual(mockChartResponse);
      expect(component.loading).toBeFalse();
    });

    it('should map chart labels and data on success', () => {
      component.jobId = 'job-1';
      component.loadChart();
      expect(component.lineChartLabels).toEqual(['1', '2']);
      expect(component.lineChartData).toEqual([{
        label: 'Run Duration (ms)',
        data: mockChartResponse.series[0].data
      }]);
    });

    it('should call renderChart inside a setTimeout on success', fakeAsync(() => {
      component.jobId = 'job-1';
      component.loadChart();
      expect(component['renderChart']).not.toHaveBeenCalled();
      tick();
      expect(component['renderChart']).toHaveBeenCalled();
    }));

    it('should set error message and loading to false on failure', () => {
      mockPerformanceChartService.getApiPerformanceChart.and.returnValue(throwError(() => new Error('Network error')));
      component.jobId = 'job-1';
      component.loadChart();
      expect(component.error).toBe('Failed to load chart');
      expect(component.loading).toBeFalse();
    });

    it('should not set chartResponse on failure', () => {
      mockPerformanceChartService.getApiPerformanceChart.and.returnValue(throwError(() => new Error('Network error')));
      component.chartResponse = undefined;
      component.jobId = 'job-1';
      component.loadChart();
      expect(component.chartResponse).toBeUndefined();
    });
  });

  describe('mapToChart', () => {
    it('should set empty labels and data when series array is empty', () => {
      component['mapToChart']({ title: 'Test', xAxis: 'run', series: [] });
      expect(component.lineChartLabels).toEqual([]);
      expect(component.lineChartData).toEqual([]);
    });

    it('should set empty labels and data when series is undefined', () => {
      component['mapToChart']({ title: 'Test', xAxis: 'run', series: undefined as any });
      expect(component.lineChartLabels).toEqual([]);
      expect(component.lineChartData).toEqual([]);
    });

    it('should map labels from the first series data point labels', () => {
      component['mapToChart'](mockChartResponse);
      expect(component.lineChartLabels).toEqual(['1', '2']);
    });

    it('should map all series to chart data with label and data', () => {
      const multiSeriesResponse: ChartResponse = {
        title: 'Test',
        xAxis: 'run',
        series: [
          { name: 'Series A', data: [{ label: '1', value: 100, status: 'PASS' }] },
          { name: 'Series B', data: [{ label: '1', value: 200, status: 'FAIL' }] }
        ]
      };
      component['mapToChart'](multiSeriesResponse);
      expect(component.lineChartData.length).toBe(2);
      expect(component.lineChartData[0]).toEqual({ label: 'Series A', data: multiSeriesResponse.series[0].data });
      expect(component.lineChartData[1]).toEqual({ label: 'Series B', data: multiSeriesResponse.series[1].data });
    });
  });
});

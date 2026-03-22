import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { DashboardComponent } from './dashboard-component';
import { TestRunService } from '../services/test-run.service';
import { PerformanceChartService } from '../services/performance-chart.service';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;

  const mockTestRunService = {
      getRecentActivity: jasmine.createSpy('getRecentActivity').and.returnValue(
        of([
          {
            runId: 'run-1',
            jobId: 'job-1',
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

    const mockPerformanceChartService = {
      getApiPerformanceChart: jasmine.createSpy('getApiPerformanceChart').and.returnValue(
        of({
          title: 'Endpoint_Insight_Health API Performance',
          xAxis: 'runNumber',
          series: [
            {
              name: 'Run Duration (ms)',
              data: [
                { label: '1', value: 6469 },
                { label: '2', value: 6254 }
              ]
            }
          ]
        })
      )
    };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DashboardComponent],
            providers: [
              { provide: TestRunService, useValue: mockTestRunService },
              { provide: PerformanceChartService, useValue: mockPerformanceChartService }
            ]
    }).compileComponents();

    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('loads recent activity on init', () => {
    expect(mockTestRunService.getRecentActivity).toHaveBeenCalledWith(10);
    expect(component.tests.length).toBe(1);
    expect(component.tests[0].testName).toBe('Endpoint_Insight_Health');
    expect(component.tests[0].status).toBe('PASS');
  });

  it('loads chart for the most recent passing run', () => {
      expect(mockPerformanceChartService.getApiPerformanceChart).toHaveBeenCalled();
    expect(mockPerformanceChartService.getApiPerformanceChart).toHaveBeenCalledWith('job-1');
  });

  it('maps chart response into labels and data', () => {
    expect(component.lineChartLabels).toEqual(['1', '2']);
    expect(component.lineChartData).toEqual([
      {
        label: 'Run Duration (ms)',
        data: [6469, 6254]
      }
    ]);
  });
});

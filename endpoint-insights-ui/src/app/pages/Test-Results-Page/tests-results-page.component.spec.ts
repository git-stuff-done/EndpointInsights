import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { ActivatedRoute, Router } from '@angular/router';

import { TestsResultsPageComponent } from './tests-results-page.component';
import { TestRunService } from '../../services/test-run.service';
import { RecentActivity } from '../../models/test-run.model';

describe('TestsResultsPageComponent', () => {
  let fixture: ComponentFixture<TestsResultsPageComponent>;
  let serviceSpy: jasmine.SpyObj<TestRunService>;

  beforeEach(async () => {
    serviceSpy = jasmine.createSpyObj('TestRunService', ['getRecentActivity']);

    await TestBed.configureTestingModule({
      imports: [TestsResultsPageComponent],
      providers: [
        { provide: TestRunService, useValue: serviceSpy },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { queryParamMap: { get: () => null } } },
        },
        {
          provide: Router,
          useValue: { navigate: jasmine.createSpy('navigate') },
        },
      ],
    }).compileComponents();
  });

  it('should create', () => {
    serviceSpy.getRecentActivity.and.returnValue(of([]));

    fixture = TestBed.createComponent(TestsResultsPageComponent);
    fixture.detectChanges();

    expect(fixture.componentInstance).toBeTruthy();
  });

  it('shows empty state when no runs', () => {
    serviceSpy.getRecentActivity.and.returnValue(of([]));

    fixture = TestBed.createComponent(TestsResultsPageComponent);
    fixture.detectChanges();

    expect(fixture.componentInstance.isLoading).toBeFalse();
    expect(fixture.componentInstance.loadError).toBeNull();
  });

  it('renders a run row', () => {
    const run: RecentActivity = {
      runId: 'run-1',
      jobId: 'job-1',
      testName: 'Vision API',
      group: 'Daily',
      dateRun: new Date().toISOString(),
      durationMs: 500,
      startedBy: 'alice',
      status: 'PASS',
    };
    serviceSpy.getRecentActivity.and.returnValue(of([run]));

    fixture = TestBed.createComponent(TestsResultsPageComponent);
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent || '';
    expect(text).toContain('Vision API');
    expect(text).toContain('run-1');
    expect(text).toContain('alice');
    expect(text).toContain('PASS');
  });

  it('shows error message when service fails', () => {
    serviceSpy.getRecentActivity.and.returnValue(throwError(() => new Error('fail')));

    fixture = TestBed.createComponent(TestsResultsPageComponent);
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent || '';
    expect(text).toContain('Unable to load test results.');
  });
});

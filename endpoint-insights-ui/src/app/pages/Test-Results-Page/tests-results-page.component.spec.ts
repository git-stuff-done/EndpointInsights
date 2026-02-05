import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';

import { TestsResultsPageComponent } from './tests-results-page.component';
import { TestRunService } from '../../services/test-run.service';
import { TestRun } from '../../models/test-run.model';

describe('TestsResultsPageComponent', () => {
  let fixture: ComponentFixture<TestsResultsPageComponent>;
  let serviceSpy: jasmine.SpyObj<TestRunService>;

  beforeEach(async () => {
    serviceSpy = jasmine.createSpyObj('TestRunService', ['getRecentTestRuns']);

    await TestBed.configureTestingModule({
      imports: [TestsResultsPageComponent],
      providers: [{ provide: TestRunService, useValue: serviceSpy }],
    }).compileComponents();
  });

  it('should create', () => {
    serviceSpy.getRecentTestRuns.and.returnValue(of([]));

    fixture = TestBed.createComponent(TestsResultsPageComponent);
    fixture.componentInstance.tests = [];
    fixture.detectChanges();

    expect(fixture.componentInstance).toBeTruthy();
  });

  it('shows empty state when no runs', () => {
    serviceSpy.getRecentTestRuns.and.returnValue(of([]));

    fixture = TestBed.createComponent(TestsResultsPageComponent);
    fixture.componentInstance.tests = [];
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent || '';
    expect(text).toContain('No recent runs found.');
  });

  it('renders a run row', () => {
    const run: TestRun = {
      runId: 'run-1',
      jobId: 'job-1',
      runBy: 'alice',
      status: 'PASS',
      finishedAt: new Date().toISOString(),
    };
    serviceSpy.getRecentTestRuns.and.returnValue(of([run]));

    fixture = TestBed.createComponent(TestsResultsPageComponent);
    fixture.componentInstance.tests = [];
    fixture.detectChanges();

    const rows = fixture.nativeElement.querySelectorAll('table tbody tr');
    expect(rows.length).toBe(1);

    const text = (fixture.nativeElement as HTMLElement).textContent || '';
    expect(text).toContain('run-1');
    expect(text).toContain('job-1');
    expect(text).toContain('alice');
    expect(text).toContain('PASS');
  });

  it('shows error message when service fails', () => {
    serviceSpy.getRecentTestRuns.and.returnValue(throwError(() => new Error('fail')));

    fixture = TestBed.createComponent(TestsResultsPageComponent);
    fixture.componentInstance.tests = [];
    fixture.detectChanges();

    const text = (fixture.nativeElement as HTMLElement).textContent || '';
    expect(text).toContain('Unable to load recent test runs.');
  });
});

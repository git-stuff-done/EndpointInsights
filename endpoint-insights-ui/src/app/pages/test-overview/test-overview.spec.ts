import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { TestOverview } from './test-overview';
import { CreateJobModal } from '../../components/create-job-modal/create-job-modal';
import { EditJobModal } from '../../components/edit-job-modal/edit-job-modal';
import { TestItem } from '../../models/test.model';
import { JobService } from '../../services/job-services';
import { TestRunService } from '../../services/test-run.service';
import { ToastService } from '../../services/toast.service';

const mockBackendJob = {
    jobId: 'abc-123',
    name: 'Auth - Login',
    testBatches: [{ batchName: 'Nightly-01' }],
    createdDate: '2024-01-01T00:00:00Z',
    createdBy: 'Alex',
    jobType: 'E2E',
    gitUrl: 'git.com/test',
    description: 'test description',
    runCommand: './run',
    compileCommand: './compile',
    threshold: 20,

};

const mappedTestItem: TestItem = {
    id: 'abc-123',
    name: 'Auth - Login',
    batch: 'Nightly-01',
    createdAt: '2024-01-01T00:00:00Z',
    createdBy: 'Alex',
    jobType: 'E2E',
    gitUrl: 'git.com/test',
    description: 'test description',
    runCommand: './run',
    compileCommand: './compile',
    status: 'SUCCESS',
    threshold: 20,

};

describe('TestOverview', () => {
    let component: TestOverview;
    let fixture: ComponentFixture<TestOverview>;
    let dialogSpy: jasmine.SpyObj<MatDialog>;
    let jobServiceSpy: jasmine.SpyObj<JobService>;
    let testRunServiceSpy: jasmine.SpyObj<TestRunService>;
    let toastServiceSpy: jasmine.SpyObj<ToastService>;

    beforeEach(waitForAsync(() => {
        dialogSpy = jasmine.createSpyObj('MatDialog', ['open']);
        jobServiceSpy = jasmine.createSpyObj('JobService', ['getAllJobs', 'createJob', 'updateJob', 'deleteJob', 'runJob']);
        testRunServiceSpy = jasmine.createSpyObj('TestRunService', ['getRecentActivity']);
        toastServiceSpy = jasmine.createSpyObj('ToastService', ['onSuccess', 'onError']);

        jobServiceSpy.getAllJobs.and.returnValue(of([mockBackendJob]));
        testRunServiceSpy.getRecentActivity.and.returnValue(of([{
            runId: 'run-1', jobId: 'abc-123', testName: 'Auth - Login',
            group: 'Daily', dateRun: '2024-01-01T01:00:00Z', durationMs: 500,
            startedBy: 'system', status: 'PASS',
        }]));

        TestBed.configureTestingModule({
            imports: [TestOverview],
            providers: [
                provideNoopAnimations(),
                { provide: MatDialog, useValue: dialogSpy },
                { provide: JobService, useValue: jobServiceSpy },
                { provide: TestRunService, useValue: testRunServiceSpy },
                { provide: ToastService, useValue: toastServiceSpy },
            ],
        }).compileComponents();
    }));

    beforeEach(() => {
        fixture = TestBed.createComponent(TestOverview);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should call loadTests on init and populate tests with status from recent activity', () => {
        expect(jobServiceSpy.getAllJobs).toHaveBeenCalled();
        expect(testRunServiceSpy.getRecentActivity).toHaveBeenCalledWith(100);
        expect(component.tests.length).toBe(1);
        expect(component.tests[0]).toEqual(jasmine.objectContaining(mappedTestItem));
    });

    it('onFilter logs to console', () => {
        const logSpy = spyOn(console, 'log');
        component.onFilter();
        expect(logSpy).toHaveBeenCalledWith('Filter Button clicked');
    });

    it('onOpen logs to console', () => {
        const logSpy = spyOn(console, 'log');
        const t = component.tests[0];
        component.onOpen(t);
        expect(logSpy).toHaveBeenCalledWith('Open Clicked');
    });

    it('onRun calls jobService.runJob and shows success toast', () => {
        jobServiceSpy.runJob.and.returnValue(of({}));
        const t = component.tests[0];
        component.onRun(t);
        expect(jobServiceSpy.runJob).toHaveBeenCalledWith(t.id);
        expect(toastServiceSpy.onSuccess).toHaveBeenCalledWith('Test run started!');
    });

    it('onRun shows error toast on failure', () => {
        jobServiceSpy.runJob.and.returnValue(throwError(() => new Error('fail')));
        const t = component.tests[0];
        component.onRun(t);
        expect(toastServiceSpy.onError).toHaveBeenCalledWith('Failed to start test run.');
    });

    it('onEdit calls openEditModal with the test', () => {
        const t = component.tests[0];
        const openEditSpy = spyOn(component, 'openEditModal');
        component.onEdit(t);
        expect(openEditSpy).toHaveBeenCalledWith(t);
    });

    it('onDelete calls jobService.deleteJob, removes item from tests, and shows success toast', () => {
        jobServiceSpy.deleteJob.and.returnValue(of(null));
        const t = component.tests[0];
        component.onDelete(t);
        expect(jobServiceSpy.deleteJob).toHaveBeenCalledWith(t.id);
        expect(toastServiceSpy.onSuccess).toHaveBeenCalledWith('Test deleted.');
        expect(component.tests.find(item => item.id === t.id)).toBeUndefined();
    });

    it('onDelete shows error toast on failure', () => {
        jobServiceSpy.deleteJob.and.returnValue(throwError(() => new Error('fail')));
        const t = component.tests[0];
        component.onDelete(t);
        expect(toastServiceSpy.onError).toHaveBeenCalledWith('Failed to delete test.');
    });

    it('openCreateJobModal opens CreateJobModal with correct config', () => {
        dialogSpy.open.and.returnValue({ afterClosed: () => of(null) } as any);
        component.openCreateJobModal();
        expect(dialogSpy.open).toHaveBeenCalledWith(CreateJobModal, jasmine.objectContaining({
            width: '600px',
            maxWidth: '95vw',
        }));
    });

    it('openCreateJobModal calls loadTests when dialog returns truthy result', () => {
        jobServiceSpy.getAllJobs.calls.reset();
        testRunServiceSpy.getRecentActivity.calls.reset();
        dialogSpy.open.and.returnValue({ afterClosed: () => of({ id: 'new' }) } as any);
        component.openCreateJobModal();
        expect(jobServiceSpy.getAllJobs).toHaveBeenCalled();
        expect(testRunServiceSpy.getRecentActivity).toHaveBeenCalled();
    });

    it('openCreateJobModal does not reload when dialog returns falsy result', () => {
        jobServiceSpy.getAllJobs.calls.reset();
        dialogSpy.open.and.returnValue({ afterClosed: () => of(null) } as any);
        component.openCreateJobModal();
        expect(jobServiceSpy.getAllJobs).not.toHaveBeenCalled();
    });

    it('openEditModal opens EditJobModal with correct config and data', () => {
        const t = component.tests[0];
        dialogSpy.open.and.returnValue({ afterClosed: () => of(null) } as any);
        component.openEditModal(t);
        expect(dialogSpy.open).toHaveBeenCalledWith(EditJobModal, jasmine.objectContaining({
            width: '600px',
            maxWidth: '95vw',
            data: t,
        }));
    });

    it('openEditModal calls loadTests when dialog returns truthy result', () => {
        jobServiceSpy.getAllJobs.calls.reset();
        testRunServiceSpy.getRecentActivity.calls.reset();
        const t = component.tests[0];
        dialogSpy.open.and.returnValue({ afterClosed: () => of({ updated: true }) } as any);
        component.openEditModal(t);
        expect(jobServiceSpy.getAllJobs).toHaveBeenCalled();
        expect(testRunServiceSpy.getRecentActivity).toHaveBeenCalled();
    });

    it('openEditModal does not reload when dialog returns falsy result', () => {
        jobServiceSpy.getAllJobs.calls.reset();
        const t = component.tests[0];
        dialogSpy.open.and.returnValue({ afterClosed: () => of(null) } as any);
        component.openEditModal(t);
        expect(jobServiceSpy.getAllJobs).not.toHaveBeenCalled();
    });
});

import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {MatDialogRef} from '@angular/material/dialog';
import {provideNoopAnimations} from '@angular/platform-browser/animations';
import {CreateJobModal} from './create-job-modal';
import {DebugElement} from '@angular/core';
import {By} from '@angular/platform-browser';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';
import { JobService } from '../../services/job-services';
import { ToastService } from '../../services/toast.service';

describe('CreateJobModal', () => {
    let component: CreateJobModal;
    let fixture: ComponentFixture<CreateJobModal>;
    let mockDialogRef: jasmine.SpyObj<MatDialogRef<CreateJobModal>>;
    let jobServiceSpy: jasmine.SpyObj<JobService>;
    let toastServiceSpy: jasmine.SpyObj<ToastService>;
    let compiled: DebugElement;

    beforeEach(async () => {
        mockDialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);
        jobServiceSpy = jasmine.createSpyObj('JobService', ['createJob']);
        toastServiceSpy = jasmine.createSpyObj('ToastService', ['onSuccess', 'onError']);

        await TestBed.configureTestingModule({
            imports: [CreateJobModal, HttpClientTestingModule],
            providers: [
                {provide: MatDialogRef, useValue: mockDialogRef},
                {provide: JobService, useValue: jobServiceSpy},
                {provide: ToastService, useValue: toastServiceSpy},
                provideNoopAnimations()
            ]
        })
            .compileComponents();

        fixture = TestBed.createComponent(CreateJobModal);
        component = fixture.componentInstance;
        compiled = fixture.debugElement;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    describe('Component Initialization', () => {
        it('should have a reference to MatDialogRef', () => {
            expect(component['dialogRef']).toBe(mockDialogRef);
        });

        it('should render the modal header with correct title', () => {
            const headerElement = compiled.query(By.css('h2[mat-dialog-title]'));
            expect(headerElement).toBeTruthy();
            expect(headerElement.nativeElement.textContent).toContain('Create New Job');
        });

        it('should render the CreateJobForm component', () => {
            const jobFormElement = compiled.query(By.css('app-job-form'));
            expect(jobFormElement).toBeTruthy();
        });

        it('should render Cancel button', () => {
            const cancelButton = compiled.query(By.css('.create-job-modal-cancel-button'));
            expect(cancelButton).toBeTruthy();
            expect(cancelButton.nativeElement.textContent).toContain('Cancel');
        });

        it('should render Create Job button', () => {
            const createButton = compiled.query(By.css('.create-job-modal-create-button'));
            expect(createButton).toBeTruthy();
            expect(createButton.nativeElement.textContent).toContain('Create Job');
        });
    });

    describe('onSubmit', () => {
        it('should call submitForm when Create Job button is clicked', () => {
            const mockCreateJobForm = jasmine.createSpyObj('CreateJobForm', ['submitForm']);
            component.createJobForm = mockCreateJobForm;

            const createButton = compiled.query(By.css('.create-job-modal-create-button'));
            createButton.nativeElement.click();

            expect(mockCreateJobForm.submitForm).toHaveBeenCalledTimes(1);
        });

        it('should call jobService.createJob with remapped payload', () => {
            jobServiceSpy.createJob.and.returnValue(of({} as any));
            component.onSubmit({ jobType: 'PERF', name: 'Test' });
            expect(jobServiceSpy.createJob).toHaveBeenCalledWith(
                jasmine.objectContaining({ testType: 'PERF', name: 'Test' })
            );
        });

        it('should close the dialog with the response on success', () => {
            const response = { jobId: 'abc' };
            jobServiceSpy.createJob.and.returnValue(of(response as any));
            component.onSubmit({ jobType: 'E2E', name: 'Test' });
            expect(mockDialogRef.close).toHaveBeenCalledWith(response);
        });

        it('should show success toast on success', () => {
            jobServiceSpy.createJob.and.returnValue(of({} as any));
            component.onSubmit({ jobType: 'E2E', name: 'Test' });
            expect(toastServiceSpy.onSuccess).toHaveBeenCalledWith('Job created successfully!');
        });

        it('should show error toast on failure', () => {
            jobServiceSpy.createJob.and.returnValue(throwError(() => new Error('fail')));
            component.onSubmit({ jobType: 'E2E', name: 'Test' });
            expect(toastServiceSpy.onError).toHaveBeenCalledWith('Failed to create job. Please try again.');
        });

        it('should close the dialog on failure', fakeAsync(() => {
            jobServiceSpy.createJob.and.returnValue(throwError(() => new Error('fail')));

            component.onSubmit({ jobType: 'E2E', name: 'Test' });
            tick();

            expect(mockDialogRef.close).toHaveBeenCalledTimes(1);
        }));
    });

    describe('onCancel', () => {
        it('should close the dialog without data when onCancel is called', () => {
            component.onCancel();

            expect(mockDialogRef.close).toHaveBeenCalledWith();
            expect(mockDialogRef.close).toHaveBeenCalledTimes(1);
        });

        it('should close the dialog when Cancel button is clicked', () => {
            const cancelButton = compiled.query(By.css('.create-job-modal-cancel-button'));
            cancelButton.nativeElement.click();

            expect(mockDialogRef.close).toHaveBeenCalledTimes(1);
        });
    });

    describe('Template Integration', () => {
        it('should bind jobSubmitted event from CreateJobForm to onSubmit method', () => {
            spyOn(component, 'onSubmit');
            const mockJobData = {id: '456', name: 'New Job'};

            const jobFormElement = compiled.query(By.css('app-job-form'));
            jobFormElement.triggerEventHandler('jobSubmitted', mockJobData);

            expect(component.onSubmit).toHaveBeenCalledWith(mockJobData);
        });

        it('should have mat-dialog-content element', () => {
            const dialogContent = compiled.query(By.css('mat-dialog-content'));
            expect(dialogContent).toBeTruthy();
        });

        it('should have mat-dialog-actions element with correct alignment', () => {
            const dialogActions = compiled.query(By.css('mat-dialog-actions[align="end"]'));
            expect(dialogActions).toBeTruthy();
        });

        it('should have button-group container', () => {
            const buttonGroup = compiled.query(By.css('.button-group'));
            expect(buttonGroup).toBeTruthy();
        });
    });

    describe('ViewChild Reference', () => {
        it('should have createJobForm ViewChild property', () => {
            expect(component.createJobForm).toBeDefined();
        });
    });
});
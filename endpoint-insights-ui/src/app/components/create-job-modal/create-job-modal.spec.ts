import {ComponentFixture, TestBed} from '@angular/core/testing';
import {MatDialogRef} from '@angular/material/dialog';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {CreateJobModal} from './create-job-modal';
import {CreateJobForm} from '../create-job-form/create-job-form';
import {DebugElement} from '@angular/core';
import {By} from '@angular/platform-browser';

describe('CreateJobModal', () => {
    let component: CreateJobModal;
    let fixture: ComponentFixture<CreateJobModal>;
    let mockDialogRef: jasmine.SpyObj<MatDialogRef<CreateJobModal>>;
    let compiled: DebugElement;

    beforeEach(async () => {
        // Create a mock MatDialogRef
        mockDialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);

        await TestBed.configureTestingModule({
            imports: [CreateJobModal, BrowserAnimationsModule],
            providers: [
                {provide: MatDialogRef, useValue: mockDialogRef}
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
        it('should call submitForm on the CreateJobForm when onSubmit is called', () => {
            const mockCreateJobForm = jasmine.createSpyObj('CreateJobForm', ['submitForm']);
            component.createJobForm = mockCreateJobForm;

            component.onSubmit();

            expect(mockCreateJobForm.submitForm).toHaveBeenCalledTimes(1);
        });

        it('should call submitForm when Create Job button is clicked', () => {
            const mockCreateJobForm = jasmine.createSpyObj('CreateJobForm', ['submitForm']);
            component.createJobForm = mockCreateJobForm;

            const createButton = compiled.query(By.css('.create-job-modal-create-button'));
            createButton.nativeElement.click();

            expect(mockCreateJobForm.submitForm).toHaveBeenCalledTimes(1);
        });
    });

    describe('onJobCreated', () => {
        it('should close the dialog with job data when onJobCreated is called', () => {
            const mockJobData = {
                id: '123',
                name: 'Test Job',
                description: 'Test Description'
            };

            component.onJobCreated(mockJobData);

            expect(mockDialogRef.close).toHaveBeenCalledWith(mockJobData);
            expect(mockDialogRef.close).toHaveBeenCalledTimes(1);
        });

        it('should handle null job data', () => {
            component.onJobCreated(null);

            expect(mockDialogRef.close).toHaveBeenCalledWith(null);
            expect(mockDialogRef.close).toHaveBeenCalledTimes(1);
        });

        it('should handle undefined job data', () => {
            component.onJobCreated(undefined);

            expect(mockDialogRef.close).toHaveBeenCalledWith(undefined);
            expect(mockDialogRef.close).toHaveBeenCalledTimes(1);
        });
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
        it('should bind jobSubmitted event from CreateJobForm to onJobCreated method', () => {
            spyOn(component, 'onJobCreated');
            const mockJobData = {id: '456', name: 'New Job'};

            const jobFormElement = compiled.query(By.css('app-job-form'));
            jobFormElement.triggerEventHandler('jobSubmitted', mockJobData);

            expect(component.onJobCreated).toHaveBeenCalledWith(mockJobData);
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
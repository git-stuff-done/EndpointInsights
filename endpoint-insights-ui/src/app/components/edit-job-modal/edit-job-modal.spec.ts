import {ComponentFixture, TestBed} from '@angular/core/testing';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {provideNoopAnimations} from '@angular/platform-browser/animations';
import {EditJobModal} from './edit-job-modal';
import {DebugElement} from '@angular/core';
import {By} from '@angular/platform-browser';
import { MOCK_TESTS } from '../../models/test.model';
import { of, throwError } from 'rxjs';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ToastService } from '../../services/toast.service';

describe('EditJobModal', () => {
    let component: EditJobModal;
    let fixture: ComponentFixture<EditJobModal>;
    let mockDialogRef: jasmine.SpyObj<MatDialogRef<EditJobModal>>;
    let mockToastService: jasmine.SpyObj<ToastService>;
    let compiled: DebugElement;

    beforeEach(async () => {
        mockDialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);
        mockToastService = jasmine.createSpyObj('ToastService', ['onSuccess', 'onError']);

        await TestBed.configureTestingModule({
            imports: [EditJobModal, HttpClientTestingModule],
            providers: [
                {provide: MatDialogRef, useValue: mockDialogRef},
                {provide: MAT_DIALOG_DATA, useValue: MOCK_TESTS[0]},
                {provide: ToastService, useValue: mockToastService},
                provideNoopAnimations()
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(EditJobModal);
        component = fixture.componentInstance;
        compiled = fixture.debugElement;
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    describe("Edit Mode Toggle", () => {
        it("should set inEditMode to true on first call", () => {
            expect(component.state().inEditMode).toBeFalse();
            component.toggleEditMode();
            expect(component.state().inEditMode).toBeTrue();
        });

        it("should call createJobForm.submitForm on second call", () => {
            const mockForm = jasmine.createSpyObj('CreateJobForm', ['submitForm']);
            component.createJobForm = mockForm;
            component.toggleEditMode();
            component.toggleEditMode();
            expect(mockForm.submitForm).toHaveBeenCalled();
        });
    });

    describe("onUpdate", () => {
        it("should call updateJob when onUpdate is called", () => {
            const payload = MOCK_TESTS[0];
            spyOn(component['jobService'], 'updateJob').and.returnValue(of(payload));
            component.onUpdate(MOCK_TESTS[0]);
            expect(component['jobService'].updateJob).toHaveBeenCalledWith(component.data.jobId, MOCK_TESTS[0]);
        });
        it("Should show invalid form toast if form is invalid", () => {
            spyOn(component['jobService'], 'updateJob').and.returnValue(throwError(() => new Error("Invalid form")));
            expect(mockDialogRef.close).not.toHaveBeenCalled();
            component.onUpdate(MOCK_TESTS[0]);
            expect(mockToastService.onError).toHaveBeenCalledWith("Failed to update job");
        });
    });

    describe("onCancel", () => {
        it("should close the dialog when onCancel is called", () => {
            component.onCancel();
            expect(mockDialogRef.close).toHaveBeenCalled();
        });
    });
});
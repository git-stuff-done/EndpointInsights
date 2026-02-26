import {ComponentFixture, TestBed} from '@angular/core/testing';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {provideNoopAnimations} from '@angular/platform-browser/animations';
import {EditJobModal} from './edit-job-modal';
import {DebugElement} from '@angular/core';
import {By} from '@angular/platform-browser';
import { MOCK_TESTS } from '../../models/test.model';
import { of, throwError } from 'rxjs';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('EditJobModal', () => {
    let component: EditJobModal;
    let fixture: ComponentFixture<EditJobModal>;
    let mockDialogRef: jasmine.SpyObj<MatDialogRef<EditJobModal>>;
    let compiled: DebugElement;

    beforeEach(async () => {
        mockDialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);

        await TestBed.configureTestingModule({
            imports: [EditJobModal, HttpClientTestingModule],
            providers: [
                {provide: MatDialogRef, useValue: mockDialogRef},
                {provide: MAT_DIALOG_DATA, useValue: MOCK_TESTS[0]},
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
        it("should toggle edit mode and call onUpdate when toggling off edit mode", () => {
            spyOn(component, 'onUpdate');
            expect(component.state().inEditMode).toBeFalse();
            component.toggleEditMode();
            expect(component.state().inEditMode).toBeTrue();
            component.toggleEditMode();
            expect(component.state().inEditMode).toBeFalse();
            expect(component.onUpdate).toHaveBeenCalled();
        });
    });

    describe("onUpdate", () => {
        it("should call updateJob when onUpdate is called", () => {
            const payload = MOCK_TESTS[0];
            spyOn(component['jobService'], 'updateJob').and.returnValue(of(payload));
            component.onUpdate(MOCK_TESTS[0]);
            expect(component['jobService'].updateJob).toHaveBeenCalledWith(component.data.id, MOCK_TESTS[0]);
        });
        it("Should show invalid form toast if form is invalid", () => {
            spyOn(component['jobService'], 'updateJob').and.returnValue(throwError(() => new Error("Invalid form")));
            expect(mockDialogRef.close).not.toHaveBeenCalled();
            spyOn(component['toastService'], 'onError');
            component.onUpdate(MOCK_TESTS[0]);
            expect(component['toastService'].onError).toHaveBeenCalledWith("Failed to update job");
        });
    });

    describe("onCancel", () => {
        it("should close the dialog when onCancel is called", () => {
            component.onCancel();
            expect(mockDialogRef.close).toHaveBeenCalled();
        });
    });
});
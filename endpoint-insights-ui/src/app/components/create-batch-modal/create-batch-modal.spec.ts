import {ComponentFixture, TestBed} from '@angular/core/testing';
import {MatDialogRef} from '@angular/material/dialog';
import {provideNoopAnimations} from '@angular/platform-browser/animations';
import {CreateBatchModal} from './create-batch-modal';
import {DebugElement} from '@angular/core';
import {By} from '@angular/platform-browser';

describe('CreateBatchModal', () => {
    let component: CreateBatchModal;
    let fixture: ComponentFixture<CreateBatchModal>;
    let mockDialogRef: jasmine.SpyObj<MatDialogRef<CreateBatchModal>>;
    let compiled: DebugElement;

    beforeEach(async () => {
        mockDialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);

        await TestBed.configureTestingModule({
            imports: [CreateBatchModal,],
            providers: [
                {provide: MatDialogRef, useValue: mockDialogRef},
                provideNoopAnimations()
            ]
        })
            .compileComponents();

        fixture = TestBed.createComponent(CreateBatchModal);
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
            expect(headerElement.nativeElement.textContent).toContain('Create New Batch');
        });

        it('should render the CreateBatchForm component', () => {
            const batchFormElement = compiled.query(By.css('app-batch-form'));
            expect(batchFormElement).toBeTruthy();
        });

        it('should render Cancel button', () => {
            const cancelButton = compiled.query(By.css('.create-batch-modal-cancel-button'));
            expect(cancelButton).toBeTruthy();
            expect(cancelButton.nativeElement.textContent).toContain('Cancel');
        });

        it('should render Create Batch button', () => {
            const createButton = compiled.query(By.css('.create-batch-modal-create-button'));
            expect(createButton).toBeTruthy();
            expect(createButton.nativeElement.textContent).toContain('Create Batch');
        });
    });



    describe('onBatchCreated', () => {
        it('should close the dialog with batch data when onBatchCreated is called', () => {
            const mockBatchData = {
                id: '123',
                name: 'Test Batch',
                description: 'Test Description'
            };

            component.onBatchCreated(mockBatchData);

            expect(mockDialogRef.close).toHaveBeenCalledWith(mockBatchData);
            expect(mockDialogRef.close).toHaveBeenCalledTimes(1);
        });

        it('should handle null batch data', () => {
            component.onBatchCreated(null);

            expect(mockDialogRef.close).toHaveBeenCalledWith(null);
            expect(mockDialogRef.close).toHaveBeenCalledTimes(1);
        });

        it('should handle undefined batch data', () => {
            component.onBatchCreated(undefined);

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
            const cancelButton = compiled.query(By.css('.create-batch-modal-cancel-button'));
            cancelButton.nativeElement.click();

            expect(mockDialogRef.close).toHaveBeenCalledTimes(1);
        });
    });

    describe('Template Integration', () => {
        it('should bind batchSubmitted event from CreateBatchForm to onBatchCreated method', () => {
            spyOn(component, 'onBatchCreated');
            const mockBatchData = {id: '456', name: 'New Batch'};

            const batchFormElement = compiled.query(By.css('app-batch-form'));
            batchFormElement.triggerEventHandler('batchSubmitted', mockBatchData);

            expect(component.onBatchCreated).toHaveBeenCalledWith(mockBatchData);
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
});
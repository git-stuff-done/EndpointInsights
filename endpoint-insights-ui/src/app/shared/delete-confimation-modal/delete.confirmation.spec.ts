
import { of, throwError } from 'rxjs';
import { HttpResponse } from '@angular/common/http';
import {DeleteBatchModalComponent} from "./delete-confirmation-component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {BatchService} from "../../services/batch.service";
import {ToastService} from "../../services/toast.service";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {Batch} from "../../models/batch.model";

describe('DeleteBatchModalComponent', () => {
    let component: DeleteBatchModalComponent;
    let fixture: ComponentFixture<DeleteBatchModalComponent>;
    let batchServiceSpy: jasmine.SpyObj<BatchService>;
    let toastServiceSpy: jasmine.SpyObj<ToastService>;
    let dialogRefSpy: jasmine.SpyObj<MatDialogRef<DeleteBatchModalComponent>>;

    const mockBatch: Batch = {
        active: false, description: "", isNew: false,
        id: '123',
        batchName: 'Test Batch',
        startTime: '',
        lastRunTime: '',
        cronExpression: '',
        notificationList: [],
        jobs: []

    };

    beforeEach(async () => {
        batchServiceSpy = jasmine.createSpyObj('BatchService', ['deleteBatch']);
        toastServiceSpy = jasmine.createSpyObj('ToastService', ['onSuccess', 'onError']);
        dialogRefSpy = jasmine.createSpyObj('MatDialogRef', ['close']);

        await TestBed.configureTestingModule({
            imports: [DeleteBatchModalComponent],
            providers: [
                { provide: BatchService, useValue: batchServiceSpy },
                { provide: ToastService, useValue: toastServiceSpy },
                { provide: MatDialogRef, useValue: dialogRefSpy },
                { provide: MAT_DIALOG_DATA, useValue: mockBatch }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(DeleteBatchModalComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should have batch data injected', () => {
        expect(component.data).toEqual(mockBatch);
    });

    it('should call deleteBatch on success, show success toast and close dialog', () => {
        batchServiceSpy.deleteBatch.and.returnValue(of({} as HttpResponse<Batch>));

        component.deleteBatch();

        expect(batchServiceSpy.deleteBatch).toHaveBeenCalledWith(mockBatch);
        expect(toastServiceSpy.onSuccess).toHaveBeenCalledWith('Successfully deleted batch');
        expect(dialogRefSpy.close).toHaveBeenCalledWith(true);
    });

    it('should show error toast when delete fails', () => {
        batchServiceSpy.deleteBatch.and.returnValue(throwError(() => new Error('Delete failed')));

        component.deleteBatch();

        expect(toastServiceSpy.onError).toHaveBeenCalledWith('Failed to delete batch');
        expect(dialogRefSpy.close).not.toHaveBeenCalled();
    });

    it('should not close dialog on error', () => {
        batchServiceSpy.deleteBatch.and.returnValue(throwError(() => new Error('error')));

        component.deleteBatch();

        expect(dialogRefSpy.close).not.toHaveBeenCalled();
    });
});
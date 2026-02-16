import {Component, inject, ViewChild} from '@angular/core';
import {MatDialogActions, MatDialogContent, MatDialogRef} from "@angular/material/dialog";
import {MatDialogTitle} from "@angular/material/dialog";
import {MatButton} from "@angular/material/button";
import {CreateBatchForm} from "../create-batch-form/create-batch-form";
import {BatchService} from "../../services/batch.service";
import {ToastService} from "../../services/toast.service";

@Component({
    selector: 'app-create-batch-modal',
    standalone: true,
    imports: [
        MatDialogTitle,
        MatDialogContent,
        CreateBatchForm,
        MatDialogActions,
        MatButton
    ],
    templateUrl: './create-batch-modal.html',
    styleUrl: './create-batch-modal.scss',
})
export class CreateBatchModal {

    constructor(private dialogRef: MatDialogRef<CreateBatchModal>) {}
    private batchService = inject(BatchService);
    private toast = inject(ToastService);
    onBatchCreated(batchData: any) {
        this.batchService.saveBatch(batchData).subscribe({
            next: (data) => {
                this.toast.onSuccess("Successfully created test batch")
                this.dialogRef.close(data);
            },
            error: (err) => {
                this.toast.onError("Failed to save batch")
                console.error('Failed to save batch', err);
            }
        })
    }

    onCancel() {
        this.dialogRef.close();
    }
}

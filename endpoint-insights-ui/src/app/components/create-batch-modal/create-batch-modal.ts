import {Component, ViewChild} from '@angular/core';
import {MatDialogActions, MatDialogContent, MatDialogRef} from "@angular/material/dialog";
import {MatDialogTitle} from "@angular/material/dialog";
import {MatButton} from "@angular/material/button";
import {CreateBatchForm} from "../create-batch-form/create-batch-form";

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
    @ViewChild(CreateBatchForm) createBatchForm!: CreateBatchForm;

    constructor(
        private dialogRef: MatDialogRef<CreateBatchModal>
    ) {
    }

    onSubmit() {
        this.createBatchForm.submitForm();
    }

    onBatchCreated(batchData: any) {
        this.dialogRef.close(batchData);
    }

    onCancel() {
        this.dialogRef.close();
    }
}

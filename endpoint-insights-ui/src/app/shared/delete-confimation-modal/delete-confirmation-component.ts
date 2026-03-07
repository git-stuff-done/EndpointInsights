import {Component, inject} from "@angular/core";
import {
    MAT_DIALOG_DATA,
    MatDialogActions,
    MatDialogClose,
    MatDialogContent, MatDialogRef,
    MatDialogTitle
} from "@angular/material/dialog";
import {Batch} from "../../models/batch.model";
import {MatButton} from "@angular/material/button";
import {BatchService} from "../../services/batch.service";
import {ToastService} from "../../services/toast.service";

@Component({
    selector: 'app-delete-batch-modal',
    imports: [
        MatDialogContent,
        MatDialogActions,
        MatDialogClose,
        MatDialogTitle,
        MatButton
    ],
    templateUrl: '/delete-confirmation-component.html',
    styleUrl: '/delete-confirmation-component.scss'
})
export class DeleteBatchModalComponent {
    private batchService = inject(BatchService);
    private toast = inject(ToastService);
    private dialogRef = inject(MatDialogRef<DeleteBatchModalComponent>);

    data:Batch = inject(MAT_DIALOG_DATA);

    public constructor() {}

    deleteBatch() {
        this.batchService.deleteBatch(this.data).subscribe({
            next: () => {
                console.log('Delete succeeded');
                this.toast.onSuccess("Successfully deleted batch");
                this.dialogRef.close(true);
            },
            error: (err) => {
                console.log('Delete failed', err);
                this.toast.onError("Failed to delete batch");
            }
        });
    }
}
import {Component, Inject, signal, ViewChild} from "@angular/core";
import {
    MAT_DIALOG_DATA,
    MatDialogActions,
    MatDialogContent,
    MatDialogRef,
    MatDialogTitle
} from "@angular/material/dialog";
import {MatButton} from "@angular/material/button";
import {TestItem} from "../../models/test.model";
import {CreateJobForm} from "../create-job-form/create-job-form";
import {ToastService} from "../../services/toast.service";
import {JobService} from "../../services/job-services";

@Component({
    selector:'edit-job-modal',
    standalone:true,
    templateUrl:'edit-job-modal.html',
    styleUrl:'edit-job-modal.scss',
    imports: [
        MatDialogContent,
        MatDialogActions,
        MatButton,
        CreateJobForm,
        MatDialogTitle,
    ]
})

export class EditJobModal{
    @ViewChild(CreateJobForm) createJobForm!: CreateJobForm;
    private dialogRef: MatDialogRef<EditJobModal>;
    public state = signal({
        inEditMode: false,
    })
    constructor(
        dialogRef: MatDialogRef<EditJobModal>,
        @Inject(MAT_DIALOG_DATA) public data: TestItem,
        private toastService :ToastService,
        private jobService: JobService)
    {
        this.dialogRef = dialogRef;
    }

    toggleEditMode(){
        this.state.update(s => ({
            ...s,
            inEditMode: !s.inEditMode
        }))

        // Check for sav3e
        if (!this.state().inEditMode) {
            this.onUpdate();
        }
    }


    onUpdate(jobData?: any){
        this.jobService.updateJob(this.data.id,jobData).subscribe({
            next: (response) => {
                console.log('Job updated:', response);
                this.toastService.onSuccess('Job updated successfully!');
            },
            error: (error) => {
                console.error('Error updating job:', error);
                this.toastService.onError('Failed to update job');
            }
        });
    }

    onCancel(){
        this.dialogRef.close()
    }
}

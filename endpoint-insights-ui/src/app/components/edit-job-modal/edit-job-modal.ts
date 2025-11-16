import {Component, EventEmitter, Inject, Output, signal, ViewChild} from "@angular/core";
import {
    MAT_DIALOG_DATA,
    MatDialogActions,
    MatDialogContent,
    MatDialogRef,
    MatDialogTitle
} from "@angular/material/dialog";
import {MatButton} from "@angular/material/button";
import {TestItem} from "../../pages/test-overview/test-overview";
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
            this.onSave();
        }
    }


    onSave(){
        this.jobService.createJob(this.data).subscribe({
            next: (response) => {
                console.log('Job created:', response);
                this.toastService.onSuccess('Job created successfully!');
                // // Reset form
                // this.job = { title: '', description: '', status: 'pending' };
            },
            error: (error) => {
                console.error('Error creating job:', error);
                this.toastService.onError('Failed to create job');
            }
        });
    }

    onCancel(){
        this.dialogRef.close()
    }
}

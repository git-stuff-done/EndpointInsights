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


    getThresholdDotColor(threshold: number): string {
        if (threshold <= 99)  return '#4caf50';
        if (threshold <= 199) return '#ff9800';
        return '#f44336';
    }
    toggleEditMode(){
        if (this.state().inEditMode) {
            console.log('createJobForm:', this.createJobForm);
            console.log('form value:', this.createJobForm?.createJobForm?.value);
            if (this.createJobForm.createJobForm.invalid) {
                const controls = this.createJobForm.createJobForm.controls;
                for (const key of Object.keys(controls)) {
                    if (controls[key].invalid) {
                        console.log('invalid field:', key, controls[key].errors);
                    }
                }
                this.createJobForm.createJobForm.markAllAsTouched();
                return;
            }

            const formValue = this.createJobForm.createJobForm.value;

            this.jobService.updateJob(this.data.id, formValue).subscribe({
                next: (response) => {
                    this.toastService.onSuccess('Job updated successfully!');
                    this.data = { ...this.data, ...formValue };
                    this.state.update(s => ({ ...s, inEditMode: false }));
                    this.dialogRef.close(this.data);
                },
                error: (error) => {
                    console.error('Error updating job:', error);
                    this.toastService.onError('Failed to update job');
                }
            });

        } else {
            this.state.update(s => ({ ...s, inEditMode: true }));
        }
    }

    onUpdate(jobData?: any){ // still used by (jobSubmitted) output if needed
        this.jobService.updateJob(this.data.id, jobData).subscribe({
            next: (response) => {
                this.toastService.onSuccess('Job updated successfully!');
                this.data = { ...this.data, ...jobData };
                this.state.update(s => ({ ...s, inEditMode: false }));
                this.dialogRef.close(this.data);
            },
            error: (error) => {
                this.toastService.onError('Failed to update job');
            }
        });
    }

    onCancel(){
        this.dialogRef.close()
    }
}

import {Component, ViewChild} from '@angular/core';
import {MatDialogActions, MatDialogContent, MatDialogRef} from "@angular/material/dialog";
import {MatDialogTitle} from "@angular/material/dialog";
import {MatButton} from "@angular/material/button";
import {CreateJobForm} from "../create-job-form/create-job-form";
import {ToastService} from "../../services/toast.service";
import {JobService} from "../../services/job-services";


@Component({
    selector: 'app-create-job-modal',
    standalone: true,
    imports: [
        MatDialogTitle,
        MatDialogContent,
        CreateJobForm,
        MatDialogActions,
        MatButton
    ],
    templateUrl: './create-job-modal.html',
    styleUrl: './create-job-modal.scss',
})
export class CreateJobModal {
    @ViewChild(CreateJobForm) createJobForm!: CreateJobForm;

    constructor(
        private dialogRef: MatDialogRef<CreateJobModal>,
        private toastService: ToastService,
        private jobService: JobService
    ) {
    }

    onSubmit(jobData: any) {
        const { jobType, ...rest } = jobData;
        const payload = { ...rest, testType: jobType?.toUpperCase() };
        this.jobService.createJob(payload).subscribe({
            next: (response) => {
                this.toastService.onSuccess('Job created successfully!');
                this.dialogRef.close(response);
            },
            error: (error) => {
                console.error('Error creating job:', error);
                this.toastService.onError('Failed to create job. Please try again.');
                this.dialogRef.close();
            }
        });
    }

    onCancel() {
        this.dialogRef.close();
    }
}

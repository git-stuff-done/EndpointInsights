import {Component, ViewChild} from '@angular/core';
import { MatDialogActions, MatDialogContent, MatDialogRef} from "@angular/material/dialog";
import { MatDialogTitle} from "@angular/material/dialog";
import {MatButton} from "@angular/material/button";
import {CreateJobForm} from "../create-job-form/create-job-form";

@Component({
  selector: 'app-create-job-modal',
  standalone: true,
  imports: [
    MatDialogTitle,
    MatDialogContent,
    CreateJobForm,
    MatDialogActions,
    MatButton,
    CreateJobForm
  ],
  templateUrl: './create-job-modal.html',
  styleUrl: './create-job-modal.scss',
})
export class CreateJobModal {
  @ViewChild(CreateJobForm) createJobForm!: CreateJobForm;

  constructor(
      private dialogRef: MatDialogRef<CreateJobModal>
  ) {}

  onSubmit() {
    this.createJobForm.submitForm();
  }

  onJobCreated(jobData: any){
    this.dialogRef.close(jobData);
  }

  onCancel(){
    this.dialogRef.close();
  }
}

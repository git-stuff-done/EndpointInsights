import {Component, ViewChild} from '@angular/core';
import {MatDialog, MatDialogActions, MatDialogContent, MatDialogRef} from "@angular/material/dialog";
import {MatDialogClose, MatDialogTitle} from "@angular/material/dialog";
import {MatIcon} from "@angular/material/icon";
import {MatButton, MatIconButton} from "@angular/material/button";
import {JobForm} from "../job-form/job-form";

@Component({
  selector: 'app-create-job-modal',
  standalone: true,
  imports: [
    MatDialogTitle,
    MatDialogClose,
    MatIcon,
    MatIconButton,
    MatDialogContent,
    JobForm,
    MatDialogActions,
    MatButton
  ],
  templateUrl: './create-job-modal.html',
  styleUrl: './create-job-modal.scss',
})
export class CreateJobModal {
  @ViewChild(JobForm) jobForm!: JobForm;

  constructor(
      private dialogRef: MatDialogRef<CreateJobModal>
  ) {}

  onSubmit() {
    this.jobForm.submitForm();
  }

  onJobCreated(jobData: any){
    this.dialogRef.close(jobData);
  }

  onCancel(){
    this.dialogRef.close();
  }
}

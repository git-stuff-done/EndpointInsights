import {Component, Inject, signal, ViewChild} from "@angular/core";
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
        @Inject(MAT_DIALOG_DATA) public data: TestItem)
    {
        this.dialogRef = dialogRef;
    }

    toggleEditMode(){
        this.state.update(s => ({
            ...s,
            inEditMode: !s.inEditMode
        }))
    }

    onCancel(){
        this.dialogRef.close()
    }
}

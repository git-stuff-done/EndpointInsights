import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import {MatDialog} from "@angular/material/dialog";
import {CreateJobModal} from "../../components/create-job-modal/create-job-modal";
import {EditJobModal} from "../../components/edit-job-modal/edit-job-modal";
import {ModalComponent} from "../../shared/modal/modal.component";
import {JobStatus} from "../../common/job.constants";

export interface TestItem {
      id: string;
      name: string;
      batch: string;
      description: string,
      gitUrl: string,
      runCommand: string,
      compileCommand: string,
      jobType: string,
      createdAt: Date | string;
      createdBy: string;
      status: JobStatus;
    }

@Component({
  selector: 'app-test-overview',
  standalone: true,
  templateUrl: './test-overview.html',
  styleUrl: './test-overview.scss',
  imports: [
      CommonModule,
      MatIconModule,
      MatButtonModule,
      EditJobModal
  ],
})
export class TestOverview {
    onFilter() {
        console.log('Filter Button clicked');
      }



    tests: TestItem[] = [
      { id:'1', name:'Auth – Login OK', batch:'Nightly-01', createdAt:new Date(), createdBy:'Alex', status:'RUNNING',
      gitUrl:"git.com/test", description: "this is a test", jobType:"jmeter", compileCommand:"./ep-compile <testname>",
      runCommand:"./ep-run <testname> -<type>" },

      { id:'2', name:'Billing – Refund', batch:'Nightly-01', createdAt:new Date(), createdBy:'Sam', status:'STOPPED',
      gitUrl:"git.com/test", description: "", jobType:"nightwatch", compileCommand:"", runCommand:"./ep-run <testname> -<type>"},
    ];

    onOpen(t: TestItem)  { console.log('Open Clicked') }
    onRun(t: TestItem)   { console.log('Run Clicked') }
    onEdit(t: TestItem)  { this.openEditModal(t) }
    onDelete(t: TestItem){ console.log('Delete Clicked') }


    constructor(private dialog: MatDialog){}

    openCreateJobModal() {
        const dialogRef = this.dialog.open(CreateJobModal, {
            width: '600px',
            maxWidth: '95vw'
        });

        dialogRef.afterClosed().subscribe((result: any) => {
            if (result) {
                console.log("New job created:", result);
                //handle backend call to create new job
            }
        });
    }
    openEditModal(t:TestItem){
        const dialogRef = this.dialog.open(EditJobModal, {
            width: '600px',
            maxWidth: '95vw',
            data: t,
        });
        dialogRef.afterClosed().subscribe((result: any) => {
            if (result) {
                console.log("New job created:", result);
            }
        });
    }
}

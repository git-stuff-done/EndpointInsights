import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import {MatDialog} from "@angular/material/dialog";
import {CreateJobModal} from "../../components/create-job-modal/create-job-modal";

export interface TestItem {
      id: string;
      name: string;
      batch: string;
      createdAt: Date | string;
      createdBy: string;
      status: 'running' | 'stopped';
    }

@Component({
  selector: 'app-test-overview',
  standalone: true,
  templateUrl: './test-overview.html',
  styleUrl: './test-overview.scss',
  imports: [
      CommonModule,
      MatIconModule,
      MatButtonModule
  ],
})
export class TestOverview {
    onFilter() {
        console.log('Filter Button clicked');
      }



    tests: TestItem[] = [
      { id:'1', name:'Auth – Login OK', batch:'Nightly-01', createdAt:new Date(), createdBy:'Alex', status:'running' },
      { id:'2', name:'Billing – Refund', batch:'Nightly-01', createdAt:new Date(), createdBy:'Sam', status:'stopped' },
    ];

    onOpen(t: TestItem)  { console.log('Open Clicked') }
    onRun(t: TestItem)   { console.log('Run Clicked') }
    onEdit(t: TestItem)  { console.log('Edit Clicked') }
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
}

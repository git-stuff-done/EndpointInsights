import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import {MatDialog} from "@angular/material/dialog";
import {CreateJobModal} from "../../components/create-job-modal/create-job-modal";
import {EditJobModal} from "../../components/edit-job-modal/edit-job-modal";
import {ModalComponent} from "../../shared/modal/modal.component";
import {JobStatus} from "../../common/job.constants";
import { MOCK_TESTS, TestItem } from '../../models/test.model';

@Component({
  selector: 'app-test-overview',
  standalone: true,
  templateUrl: './test-overview.html',
  styleUrl: './test-overview.scss',
  imports: [
      CommonModule,
      MatIconModule,
      MatButtonModule,
  ],
})
export class TestOverview {
    onFilter() {
        console.log('Filter Button clicked');
      }



    tests: TestItem[] = MOCK_TESTS;

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

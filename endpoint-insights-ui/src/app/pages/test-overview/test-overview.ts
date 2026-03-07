import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import {MatDialog} from "@angular/material/dialog";
import {CreateJobModal} from "../../components/create-job-modal/create-job-modal";
import {EditJobModal} from "../../components/edit-job-modal/edit-job-modal";
import {MOCK_TESTS, TestItem} from "../../models/test.model";

@Component({
  selector: 'app-test-overview',
  standalone: true,
  templateUrl: './test-overview.html',
  styleUrl: './test-overview.scss',
  imports: [
      CommonModule,
      MatIconModule,
      MatButtonModule,
      ReactiveFormsModule,
      MatFormFieldModule,
      MatInputModule,
  ],
})
export class TestOverview {
    tests: TestItem[] = MOCK_TESTS;
    searchControl = new FormControl('');

    get filteredTests(): TestItem[] {
        const term = (this.searchControl.value ?? '').toLowerCase();
        if (!term) return this.tests;
        return this.tests.filter(t =>
            t.name.toLowerCase().includes(term) ||
            (t.description ?? '').toLowerCase().includes(term)
        );
    }

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

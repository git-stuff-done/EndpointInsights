import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';
import {MatDialog} from "@angular/material/dialog";
import {CreateJobModal} from "../../components/create-job-modal/create-job-modal";
import {EditJobModal} from "../../components/edit-job-modal/edit-job-modal";
import {MOCK_TESTS, TestItem} from "../../models/test.model";
import { JOB_STATUSES, JobStatus } from '../../common/job.constants';

@Component({
  selector: 'app-test-overview',
  standalone: true,
  templateUrl: './test-overview.html',
  styleUrl: './test-overview.scss',
  imports: [
      CommonModule,
      MatIconModule,
      MatButtonModule,
      MatMenuModule,
      MatBadgeModule,
      ReactiveFormsModule,
      MatFormFieldModule,
      MatInputModule,
  ],
})
export class TestOverview {
    tests: TestItem[] = MOCK_TESTS;
    searchControl = new FormControl('');
    selectedStatuses = new Set<JobStatus>();
    readonly JOB_STATUSES = JOB_STATUSES;

    get hasActiveFilters(): boolean {
        return this.selectedStatuses.size > 0;
    }

    toggleStatus(s: JobStatus) {
        if (this.selectedStatuses.has(s)) {
            this.selectedStatuses.delete(s);
        } else {
            this.selectedStatuses.add(s);
        }
    }

    isStatusSelected(s: JobStatus): boolean {
        return this.selectedStatuses.has(s);
    }

    get filteredTests(): TestItem[] {
        const term = (this.searchControl.value ?? '').toLowerCase();
        return this.tests.filter(t => {
            const matchesSearch = !term ||
                t.name.toLowerCase().includes(term) ||
                (t.description ?? '').toLowerCase().includes(term);
            const matchesStatus = this.selectedStatuses.size === 0 || this.selectedStatuses.has(t.status);
            return matchesSearch && matchesStatus;
        });
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

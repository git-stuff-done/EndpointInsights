import {Component, OnDestroy, OnInit} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormControl, ReactiveFormsModule} from '@angular/forms';
import {Subscription} from 'rxjs';
import {MatDialog} from '@angular/material/dialog';
import {MatButtonModule} from '@angular/material/button';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconModule} from '@angular/material/icon';
import {MatInputModule} from '@angular/material/input';
import {MatMenuModule} from '@angular/material/menu';
import {MatBadgeModule} from '@angular/material/badge';
import {Batch} from '../models/batch.model';
import {BatchStore} from '../services/batch-store.service';
import {BatchConfigDialogComponent} from './components/batch-config-dialog/batch-config-dialog.component';
import {BatchService} from "../services/batch.service";
import {DeleteBatchModalComponent} from "../shared/delete-confimation-modal/delete-confirmation-component";
import {NotificationService} from "../services/notification.service";
import {UserDisplayComponent} from "../components/user-display/user-display.component";
import {Router} from "@angular/router";

@Component({
    selector: 'app-batches',
    standalone: true,
    imports: [CommonModule, MatIconModule, MatButtonModule, MatMenuModule, MatBadgeModule, ReactiveFormsModule, MatFormFieldModule, MatInputModule, UserDisplayComponent],
    templateUrl: './batch-component.html',
    styleUrls: ['./batch-component.scss'],
})
export class BatchComponent implements OnInit, OnDestroy {
    private sub?: Subscription;
    batch: Batch[] = [];
    searchControl = new FormControl('');
    statusFilter: 'all' | 'active' | 'inactive' = 'all';

    constructor(private batchService: BatchService,
                private notificationService: NotificationService,
                private dialog: MatDialog,
                private router: Router) {

    }

    get hasActiveFilter(): boolean {
        return this.statusFilter !== 'all';
    }

    setStatusFilter(value: 'all' | 'active' | 'inactive') {
        this.statusFilter = value;
    }

    get filteredBatches(): Batch[] {
        const term = (this.searchControl.value ?? '').toLowerCase();
        return this.batch.filter(b => {
            const matchesSearch = !term || b.batchName.toLowerCase().includes(term);
            const matchesStatus = this.statusFilter === 'all' ||
                (this.statusFilter === 'active' ? b.active : !b.active);
            return matchesSearch && matchesStatus;
        });
    }

    ngOnInit() {
        this.batchService.getAllBatches().subscribe({
            next: (data) => this.batch = data.body ?? [],
            error: (err) => console.error('Error:', err)
        });
    }
    ngOnDestroy() { this.sub?.unsubscribe(); }

    loadBatches() {
        this.batchService.getAllBatches().subscribe({
            next: (data) => this.batch = data.body ?? [],
            error: (err) => console.error('Error:', err)
        });
    }

    trackById = (_: number, b: Batch) => b.id;

    onConfigure(batch: Batch) {
        this.dialog.open(BatchConfigDialogComponent, {
            width: '900px',
            height:'auto',
            data: batch
        }).afterClosed().subscribe(() => {

            this.loadBatches();
        });
    }

    runBatch(batch: Batch) {
        this.batchService.runBatch(batch).subscribe({
            next: (data) => {
                this.notificationService.showToast(`Run started with id: ${data.body?.runId}`, 'success');
            },
            error: (err) => this.notificationService.showToast(`Failed to run batch: ${err.error.details[0]}`, 'error')
        })
    }

    onDelete(batch: Batch){
        this.dialog.open(DeleteBatchModalComponent, {
            width: '400px',
            height:'auto',
            data: batch
        }).afterClosed().subscribe(confirmed => {
            console.log('Dialog closed, confirmed:', confirmed);
            if (confirmed) {
                console.log('Calling loadBatches');
                this.loadBatches();
            }
        });
    }
    onFilter() { console.log('Filter Button clicked'); }

    openCreateBatchModal() {
        this.dialog.open(BatchConfigDialogComponent, {
            width: '900px',
            height: 'auto',
            data: {
                id: "",
                batchName: '',
                startTime: '',
                active: false,
                lastRunTime: '',
 //               scheduledDays: [],
                nextRunTime: '',
                nextRunDate: '',
                notificationList: [],
                jobs: [],
                isNew: true,
            } as Batch
        }).afterClosed().subscribe((result: any) => {
            if (result) {
                this.loadBatches();
            }
        });
    }

    protected onView(b: Batch) {
        this.router.navigate(['/test-results'], { queryParams: { name: b.batchName }, state: { displayGraph: true, batchId: b.id } });
    }
}

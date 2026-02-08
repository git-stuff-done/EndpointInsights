import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';

import { BatchCardComponent } from './components/batch-card/batch-card.component';
import { Batch } from '../models/batch.model';
import { BatchStore } from '../services/batch-store.service';
import { BatchConfigDialogComponent } from './components/batch-config-dialog/batch-config-dialog.component';
import {BatchService} from "../services/batch.service";

@Component({
    selector: 'app-batches',
    standalone: true,
    imports: [CommonModule, BatchCardComponent],
    templateUrl: './batch-component.html',
    styleUrls: ['./batch-component.scss'],
})
export class BatchComponent implements OnInit, OnDestroy {
    private readonly store = inject(BatchStore);
    private readonly dialog = inject(MatDialog);
    private batchService = inject(BatchService);
    private sub?: Subscription;

    batch: Batch[] = [];

    ngOnInit() {
        this.batchService.getAllBatches().subscribe({
            next: (data) => this.batch = data,
            error: (err) => console.error('Error:', err)
        });
    }
    ngOnDestroy() { this.sub?.unsubscribe(); }

    loadBatches() {
        this.batchService.getAllBatches().subscribe({
            next: (data) => this.batch = data,
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
}

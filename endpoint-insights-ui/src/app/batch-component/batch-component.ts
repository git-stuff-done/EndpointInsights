import { Component, inject, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Subscription } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import {CreateBatchModal} from "../components/create-batch-modal/create-batch-modal";
import { BatchCardComponent } from './components/batch-card/batch-card.component';
import { Batch } from '../models/batch.model';
import { BatchStore } from '../services/batch-store.service';
import { BatchConfigDialogComponent } from '../shared/modal/batch-config-dialog.component';

@Component({
    selector: 'app-batches',
    standalone: true,
    imports: [CommonModule, BatchCardComponent, MatIconModule, MatButtonModule],
    templateUrl: './batch-component.html',
    styleUrls: ['./batch-component.scss'],
})
export class BatchComponent implements OnInit, OnDestroy {
    private readonly store = inject(BatchStore);
    private readonly dialog = inject(MatDialog);
    private sub?: Subscription;

    batch: Batch[] = [];

    ngOnInit() {
        this.sub = this.store.batches$.subscribe(list => this.batch = list);
    }
    ngOnDestroy() { this.sub?.unsubscribe(); }

    trackById = (_: number, b: Batch) => b.id;

    onConfigure(batch: Batch) {
        this.dialog.open(BatchConfigDialogComponent, {
            width: '720px',
            data: { batchId: batch.id, title: batch.title }
        }).afterClosed().subscribe(result => {
            if (!result) return;
            const { title, nextRunIso } = result;
            this.store.update(batch.id, {
                title: typeof title === 'string' && title.trim() ? title.trim() : batch.title,
                date: nextRunIso ?? batch.date
            });
        });
    }

    openCreateBatchModal() {
            const dialogRef = this.dialog.open(CreateBatchModal, {
            width: '600px',
            maxWidth: '95vw'
        });

        dialogRef.afterClosed().subscribe((result: any) => {
            if (result) {
                console.log("New batch created:", result);
            }
        });
    }
}

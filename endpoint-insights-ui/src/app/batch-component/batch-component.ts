import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Batch } from '../models/batch.model';
import { BatchCardComponent } from './components/batch-card/batch-card.component';

@Component({
    selector: 'app-batches',
    standalone: true,
    imports: [CommonModule, BatchCardComponent,],
    templateUrl: './batch-component.html',
    styleUrls: ['./batch-component.scss'],
})
export class BatchComponent {
  // mock data for now; I will need to fetch this from the server later
  batches: Batch[] = [
    { id: 'B-2025-00123', title: 'Nightly ETL (US-East)', createdIso: '2025-10-17T02:13:00Z' },
    { id: 'B-2025-00124', title: 'Customer Backfill – Oct', createdIso: '2025-10-18T15:45:00Z' },
  ];

  onConfigure(batch: Batch) {
    // gotta hook this up to the server later
    console.log('Configure clicked:', batch);
  }

  trackById = (_: number, b: Batch) => b.id;
}

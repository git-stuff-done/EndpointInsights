import { Component, EventEmitter, Input, Output, ChangeDetectionStrategy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Batch } from '../../../models/batch.model';

@Component({
  selector: 'app-batch-card',
  standalone: true,
  imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule],
  templateUrl: './batch-card.component.html',
  styleUrls: ['./batch-card.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BatchCardComponent {
  @Input({ required: true }) batch!: Batch;
  @Output() configure = new EventEmitter<Batch>();

  formattedDate(): string {
    const d = new Date(this.batch?.createdIso ?? '');
    return isNaN(d.valueOf()) ? '—' : d.toLocaleString();
  }
}

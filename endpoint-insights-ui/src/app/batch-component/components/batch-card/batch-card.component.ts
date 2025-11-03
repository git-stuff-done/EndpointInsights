// batch-card.component.ts
import { Component, Input, Output, EventEmitter, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ModalService } from '../../../shared/modal/modal.service';
import { Batch } from '../../../models/batch.model';
@Component({
    selector: 'app-batch-card',
    standalone: true,
    imports: [CommonModule, MatCardModule, MatButtonModule, MatIconModule],
    templateUrl: './batch-card.component.html',
    styleUrls: ['./batch-card.component.scss'],
})
export class BatchCardComponent {
    @Input() batch!: Batch;

    @Output() configure = new EventEmitter<Batch>();

    private modal = inject(ModalService);

    onConfigure() {
        // Open the reusable modal with multiple tabs
        this.modal.open({
            title: `Configure: ${this.batch.title}`,
            tabs: [
                {
                    label: 'Overview',
                    content: `
            <p><strong>Batch ID:</strong> ${this.batch.id}</p>
            <p><strong>Date:</strong> ${new Date(this.batch.date).toLocaleString()}</p>
          `,
                },
                {
                    label: 'Settings',
                    content: `
            <p>Owner, thresholds, notifications…</p>
          `,
                },
                {
                    label: 'Runs',
                    content: `
            <p>Recent executions and statuses.</p>
          `,
                },
            ],
        });

        // If parents still expect the event, keep it:
        this.configure.emit(this.batch);
    }

    formattedDate(): string {
        return new Date(this.batch.date).toLocaleString();
    }
}

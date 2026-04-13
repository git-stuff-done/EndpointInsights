// batch-card.component.ts
import { Component, Input, Output, EventEmitter, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { ModalService } from '../../../shared/modal/modal.service';
import { Batch } from '../../../models/batch.model';
import {BatchConfigDialogComponent} from "../batch-config-dialog/batch-config-dialog.component";
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
    @Output() delete = new EventEmitter<Batch>();

    onConfigure() {
       this.configure.emit(this.batch);
    }

    onDelete() {
        this.delete.emit(this.batch);
    }

    formattedDate(): string {
        console.log(this.batch.lastTimeRun)
        if (!this.batch.lastTimeRun) return '—';
        return new Date(this.batch.lastTimeRun).toLocaleString();
    }
}

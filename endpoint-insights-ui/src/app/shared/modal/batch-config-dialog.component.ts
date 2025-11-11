import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';

export interface BatchConfigData {
    batchId: string;
    title?: string;
}

@Component({
    selector: 'app-batch-config-dialog',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatDialogModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatIconModule,
        MatTabsModule,
        MatDatepickerModule,
        MatNativeDateModule,
    ],
    templateUrl: './batch-config-dialog.component.html',
    styleUrls: ['./batch-config-dialog.component.scss'],
})
export class BatchConfigDialogComponent implements OnInit {
    private readonly fb = inject(FormBuilder);
    private readonly data = inject<BatchConfigData>(MAT_DIALOG_DATA);
    private readonly dialogRef = inject(MatDialogRef<BatchConfigDialogComponent>);

    // pretend-loading; toggle to true if you fetch from backend
    loading = signal(false);

    // fetch this from the server for the selected batch
    private _testCount = signal<number | null>(null);
    testCount = () => this._testCount();

    form = this.fb.group({
        title: ['', [Validators.required, Validators.maxLength(120)]],
        nextRunDate: <Date | null>null,
        nextRunTime: [''],
    });

    ngOnInit(): void {
        this.loading.set(true);
        // Seed initial values from data (or fetch from server and patch)
        this.form.patchValue({
            title: (this.data.title ?? '').trim(),
            nextRunDate: null,
            nextRunTime: '',
        });
        this.loading.set(false);
    }

    refreshCount(): void {
        // Demo: simulate fetching test count
        const simulated = Math.floor(Math.random() * 50) + 1;
        this._testCount.set(simulated);
    }

    saveTitle(): void {
        // Hook for future PATCH /batches/:id {title}
    }

    close(): void {
        this.dialogRef.close({
            title: this.form.controls.title.value?.trim() || this.data.title || '',
            nextRunIso: this.composeIso(this.form.controls.nextRunDate.value, this.form.controls.nextRunTime.value),
        });
    }

    /** Compose an ISO string from a date control and "HH:MM" time string */
    private composeIso(date: Date | null, time: string | null): string | undefined {
        if (!date) return undefined;
        const yyyy = date.getUTCFullYear();
        const mm = String(date.getUTCMonth() + 1).padStart(2, '0');
        const dd = String(date.getUTCDate()).padStart(2, '0');

        let hh = '00', mi = '00';
        if (time && /^\d{1,2}:\d{2}$/.test(time.trim())) {
            const [h, m] = time.trim().split(':');
            hh = String(Math.min(23, Math.max(0, parseInt(h, 10) || 0))).padStart(2, '0');
            mi = String(Math.min(59, Math.max(0, parseInt(m, 10) || 0))).padStart(2, '0');
        }
        return `${yyyy}-${mm}-${dd}T${hh}:${mi}:00Z`;
    }
}

import { Component, OnInit, inject, signal, computed } from '@angular/core';
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

export interface ApiTest {
    id: string;
    name: string;
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

    // Tests currently in the batch (top list in Settings tab)
    currentBatchTests = signal<ApiTest[]>([
        { id: '1', name: 'Vision API' },
        { id: '2', name: 'Open API' },
        { id: '3', name: 'Records API' },
    ]);

    // All available tests that can be added (bottom list in Settings tab)
    availableTests = signal<ApiTest[]>([
        { id: '1', name: 'Vision API' },
        { id: '2', name: 'Open API' },
        { id: '3', name: 'Records API' },
        { id: '4', name: 'Vision Express API' },
        { id: '5', name: 'Auth API' },
        { id: '6', name: 'Payment API' },
    ]);

    // Search term for filtering available tests
    searchTerm = signal('');

    // Filtered list: excludes tests already in batch, filters by search term
    filteredAvailableTests = computed(() => {
        const search = this.searchTerm().toLowerCase();
        const currentIds = new Set(this.currentBatchTests().map(t => t.id));
        return this.availableTests()
            .filter(t => !currentIds.has(t.id))
            .filter(t => t.name.toLowerCase().includes(search));
    });

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

    // Remove a single test from the batch
    removeTest(test: ApiTest): void {
        this.currentBatchTests.update(tests => tests.filter(t => t.id !== test.id));
    }

    // Remove all tests from the batch
    clearAllTests(): void {
        this.currentBatchTests.set([]);
    }

    // Add a test to the batch
    addTest(test: ApiTest): void {
        if (!this.currentBatchTests().some(t => t.id === test.id)) {
            this.currentBatchTests.update(tests => [...tests, test]);
        }
    }

    // Check if a test is in the batch (for toggle icon in available list)
    isInBatch(test: ApiTest): boolean {
        return this.currentBatchTests().some(t => t.id === test.id);
    }

    close(): void {
        this.dialogRef.close({
            title: this.form.controls.title.value?.trim() || this.data.title || '',
            nextRunIso: this.composeIso(this.form.controls.nextRunDate.value, this.form.controls.nextRunTime.value),
            batchTests: this.currentBatchTests(),
        });
    }

    /** Compose an ISO string from a date control and "HH:MM" time string */
    private composeIso(date: Date | null, time: string | null): string | undefined {
        if (!date) return undefined;

        // Local date components selected by the user
        const yyyy = date.getFullYear();
        const mm = date.getMonth();      // zero based
        const dd = date.getDate();

        // Default time
        let hh = 0;
        let mi = 0;

        // Validate and clamp the "HH:MM" string
        if (time && /^\d{1,2}:\d{2}$/.test(time.trim())) {
            const [h, m] = time.trim().split(':');
            hh = Math.min(23, Math.max(0, parseInt(h, 10) || 0));
            mi = Math.min(59, Math.max(0, parseInt(m, 10) || 0));
        }

        // Build a proper UTC Date
        const utcDate = new Date(Date.UTC(yyyy, mm, dd, hh, mi, 0));

        return utcDate.toISOString();
    }

}

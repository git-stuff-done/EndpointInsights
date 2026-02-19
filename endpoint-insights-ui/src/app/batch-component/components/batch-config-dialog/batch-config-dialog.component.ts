import {Component, OnInit, inject, signal, computed, ViewChild} from '@angular/core';
import { CommonModule } from '@angular/common';
import {FormBuilder, FormControl, ReactiveFormsModule, Validators} from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatDatepickerModule } from '@angular/material/datepicker';
import {MatNativeDateModule, provideNativeDateAdapter} from '@angular/material/core';
import {Batch} from "../../../models/batch.model";
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {MatSelectModule} from "@angular/material/select";
import { MatListOption, MatSelectionList} from "@angular/material/list";
import {BatchService} from "../../../services/batch.service";
import {debounceTime, distinctUntilChanged, switchMap} from "rxjs";
import {UserService} from "../../../services/user.service";


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
        MatAutocompleteModule,
        MatSelectionList,
        MatListOption,
        MatSelectModule,
    ],
    providers: [provideNativeDateAdapter()],
    templateUrl: './batch-config-dialog.component.html',
    styleUrls: ['./batch-config-dialog.component.scss'],
})
export class BatchConfigDialogComponent implements OnInit {
    private readonly fb = inject(FormBuilder);
    private readonly data = inject<Batch>(MAT_DIALOG_DATA);
    private readonly batchService = inject(BatchService);
    private readonly userService = inject(UserService)
    private readonly dialogRef = inject(MatDialogRef<BatchConfigDialogComponent>);

    @ViewChild('participantList') participantList!: MatSelectionList;
    searchControl = new FormControl('');


    selectedParticipant: any = null;
    searchParticipants: User[] =[];
    activeParticipants = signal<User[]>([]);

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

    loading = signal(false);


    form = this.fb.group({
        id: [this.data.id],
        batchName: [this.data.batchName ?? '', [Validators.required, Validators.maxLength(120)]],
        startTime: [this.data.startTime ?? ''],
        lastRunTime: [this.data.lastRunTime ?? ''],
        cronExpression: [this.data.cronExpression ?? ''],
        notificationList: [this.data.notificationList ?? []],
        jobs: [this.data.jobs ?? []]
    });

    // Schedule editor state
    scheduleFrequency = signal<'daily' | 'weekly'>('daily');
    scheduleTime = signal('12:00');
    scheduleDays = signal<string[]>([]);

    readonly dayOptions = [
        { value: 'MON', label: 'Monday' },
        { value: 'TUE', label: 'Tuesday' },
        { value: 'WED', label: 'Wednesday' },
        { value: 'THU', label: 'Thursday' },
        { value: 'FRI', label: 'Friday' },
        { value: 'SAT', label: 'Saturday' },
        { value: 'SUN', label: 'Sunday' },
    ];

    onFrequencyChange(value: 'daily' | 'weekly'): void {
        this.scheduleFrequency.set(value);
        this.buildCron();
    }

    onTimeChange(value: string): void {
        this.scheduleTime.set(value);
        this.buildCron();
    }

    onDaysChange(days: string[]): void {
        this.scheduleDays.set(days);
        this.buildCron();
    }

    /** Build a cron string from the editor state and set it on the form */
    private buildCron(): void {
        const time = this.scheduleTime();
        const [hours, minutes] = time.split(':').map(Number);

        let cron: string;
        if (this.scheduleFrequency() === 'daily') {
            cron = `0 ${minutes} ${hours} * * *`;
        } else {
            const days = this.scheduleDays();
            if (days.length === 0) {
                cron = `0 ${minutes} ${hours} * * *`;
            } else {
                cron = `0 ${minutes} ${hours} * * ${days.join(',')}`;
            }
        }

        this.form.patchValue({ cronExpression: cron });
    }

    /** Parse a cron string and update the editor state to match */
    private parseCronToEditor(cron: string): void {
        if (!cron) return;

        const parts = cron.trim().split(/\s+/);
        if (parts.length < 6) return;

        const minutes = parts[1];
        const hours = parts[2];
        const dayOfWeek = parts[5];

        this.scheduleTime.set(`${hours.padStart(2, '0')}:${minutes.padStart(2, '0')}`);

        if (dayOfWeek === '*') {
            this.scheduleFrequency.set('daily');
            this.scheduleDays.set([]);
        } else {
            this.scheduleFrequency.set('weekly');
            this.scheduleDays.set(dayOfWeek.split(',').map(d => d.trim().toUpperCase()));
        }
    }


    ngOnInit(): void {
        this.loading.set(true);
        this.form.patchValue({
            id: this.data.id,
            batchName: (this.data.batchName ?? '').trim(),
            startTime: this.data.startTime,
            lastRunTime: this.data.lastRunTime,
            cronExpression: this.data.cronExpression ?? '',
            notificationList: this.data.notificationList || [],
            jobs: this.data.jobs ?? []
        });

        // Sync existing cron string into the editor
        this.parseCronToEditor(this.data.cronExpression ?? '');

        this.searchControl.valueChanges.pipe(
            debounceTime(200),
            distinctUntilChanged(),
            switchMap(query => {
                return this.userService.searchUsers(query as string);
            })
        ).subscribe(results => {
            results = results.filter((user) => !this.activeParticipants().some(active => active.id === user.id));
            this.searchParticipants = results || [];
        });

        this.populateActiveParticipants();
    }


    /* Notification methods */
    displayParticipant(participant: any): string {
        return participant ? participant.name : '';
    }

    onParticipantSelected(event: any) {
        this.selectedParticipant = event.option.value;
    }

    populateActiveParticipants() {
        this.loading.set(true);

        const notificationIds = this.form.get('notificationList')?.value || [];
        if (!notificationIds || notificationIds.length === 0) {
            this.activeParticipants.set([]);
            this.loading.set(false);
            return;
        }
        this.userService.findUsersById(notificationIds).subscribe({
            next: (users) => {
                this.activeParticipants.set(users);
                this.loading.set(false);

            },
            error: (err) => {
                this.activeParticipants.set([]);
                this.loading.set(false);
            }
        });
    }

    addParticipant() {
        if (this.selectedParticipant) {
            const currentList = this.form.get('notificationList')?.value || [];

            if (currentList.includes(this.selectedParticipant.id)) {
                this.selectedParticipant = null;
                this.searchControl.setValue('');
                return;
            }

            const updatedList = [...currentList, this.selectedParticipant.id];
            this.form.patchValue({
                notificationList: updatedList
            });

            this.populateActiveParticipants();

            this.selectedParticipant = null;
            this.searchControl.setValue('');
        }
    }

    removeParticipant() {
        const selectedIds = this.participantList.selectedOptions.selected.map(o => o.value.id);
        const currentList = this.form.get('notificationList')?.value || [];
        const updatedList = currentList.filter((id: string) => !selectedIds.includes(id));

        this.form.patchValue({
            notificationList: updatedList
        });

        this.populateActiveParticipants();
    }


    /* Jobs */

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



    save() {
        if (this.form.invalid) {
            return;
        }
        return this.batchService.saveBatch(this.form.value).subscribe({
            next: (response) => {
                this.form.patchValue({
                    id: response.id,
                    batchName: response.batchName,
                    startTime: response.startTime,
                    lastRunTime: response.lastRunTime,
                    cronExpression: response.cronExpression ?? '',
                    notificationList: response.notificationList || []
                });

                this.populateActiveParticipants();
            },
            error: (error) => console.error('Error:', error)
        });
    }


    close(): void {
        this.dialogRef.close({
            title: this.form.controls.batchName.value?.trim() || this.data.batchName || '',
            //nextRunIso: this.composeIso(this.form.controls.nextRunDate.value, this.form.controls.nextRunTime.value),
            batchTests: this.currentBatchTests(),
        });
    }


}

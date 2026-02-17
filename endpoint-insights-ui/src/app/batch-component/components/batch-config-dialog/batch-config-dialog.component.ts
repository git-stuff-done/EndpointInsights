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
import {MatButtonToggle, MatButtonToggleGroup} from "@angular/material/button-toggle";
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {MatDivider, MatListOption, MatSelectionList} from "@angular/material/list";
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
        MatButtonToggleGroup,
        MatButtonToggle,
        MatAutocompleteModule,
        MatSelectionList,
        MatListOption,
        MatDivider,
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
    isNew = !this.data.id;

    @ViewChild('participantList') participantList!: MatSelectionList;
    searchControl = new FormControl('');


    selectedParticipant: any = null;
    searchParticipants: User[] =[];
    activeParticipants = signal<User[]>([]);

    // Tests currently in the batch (top list in Settings tab)
    currentBatchTests = signal<ApiTest[]>([
        { id: 'd10e18c5-13f8-45b6-91fd-74baa0fe6834', name: 'Vision API' },
        // { id: '2', name: 'Open API' },
        // { id: '3', name: 'Records API' },
    ]);


    currentJobs = [
        {id: "d10e18c5-13f8-45b6-91fd-74baa0fe6834", name: 'Vision API', type: "E2E"}
    ]

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
        scheduledDays: [this.data.scheduledDays ?? ''],
        nextRunTime: [this.data.nextRunTime ?? ''],
        nextRunDate: [this.data.nextRunDate ?? ''],
        notificationList: [this.data.notificationList ?? []],
        jobs: [this.data.jobs ?? []]

    });


    ngOnInit(): void {
        this.loading.set(true);
        this.form.patchValue({
            id: this.data.id,
            batchName: (this.data.batchName ?? '').trim(),
            startTime: this.data.startTime,
            lastRunTime: new Date().toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' }),
            //scheduledDays: this.data.scheduledDays ?? [],
            nextRunTime: this.data.nextRunTime ?? '',
            nextRunDate: this.data.nextRunDate ?? '',
            notificationList: this.data.notificationList || [],
            jobs: this.data.jobs ?? []

        });
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
        const newBatch = {
            ...this.form.value,
            jobs: this.currentJobs,
            isNew: this.isNew
        };

        return this.batchService.saveBatch(newBatch).subscribe({
            next: (response) => {
                this.isNew = false;
                this.form.patchValue({
                    id: response.body?.id,
                    batchName: response.body?.batchName,
                    startTime: response.body?.startTime,
                    lastRunTime: response.body?.lastRunTime,
                    scheduledDays: response.body?.scheduledDays,
                    nextRunTime: response.body?.nextRunTime,
                    nextRunDate: response.body?.nextRunDate,
                    notificationList: response.body?.notificationList || [],
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

import {Component, OnInit, inject, signal, computed, ViewChild} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormBuilder, FormControl, ReactiveFormsModule, Validators} from '@angular/forms';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatTabsModule} from '@angular/material/tabs';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatNativeDateModule, provideNativeDateAdapter} from '@angular/material/core';
import {Batch} from "../../../models/batch.model";
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {MatSelectModule} from "@angular/material/select";
import {MatDivider, MatSelectionList} from "@angular/material/list";
import {BatchService} from "../../../services/batch.service";
import {debounceTime, distinctUntilChanged, switchMap} from "rxjs";
import {UserService} from "../../../services/user.service";
import {JobsApi} from "../../../jobsApi/jobsApi";
import {TestItem} from "../../../models/test.model";
import {User} from "../../../models/user.model";


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
        MatSelectModule,
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
    private readonly userService = inject(UserService);
    private readonly jobsApi = inject(JobsApi);
    private readonly dialogRef = inject(MatDialogRef<BatchConfigDialogComponent>);
    isNew = !this.data.id;

    @ViewChild('participantList') participantList!: MatSelectionList;
    searchControl = new FormControl('');


    searchParticipants: User[] = [];
    activeParticipants = signal<User[]>([]);

    currentBatchTests = signal<TestItem[]>([]);
    emailList = signal<string[]>([]);
    emailInputControl = new FormControl('');


    // All available tests that can be added (bottom list in Settings tab)
    availableTests = signal<TestItem[]>([]);

    // Search term for filtering available tests
    searchTerm = signal('');

    // Filtered list: excludes tests already in batch, filters by search term
    filteredAvailableTests = computed(() => {
        const search = this.searchTerm().toLowerCase();
        const currentIds = new Set(this.currentBatchTests().map(t => t.jobId));
        return this.availableTests()
            .filter(t => !currentIds.has(t.jobId))
            .filter(t => t.name.toLowerCase().includes(search));
    });

    loading = signal(false);


    form = this.fb.group({
        id: [this.data.id],
        batchName: [this.data.batchName ?? '', [
            Validators.required,
            Validators.minLength(3),
            Validators.maxLength(50),
            Validators.pattern(/^[a-zA-Z0-9_-]+$/),
        ]],
        startTime: [this.data.startTime ?? ''],
        lastRunTime: [this.data.lastTimeRun ?? ''],
        cronExpression: [this.data.cronExpression ?? ''],
        notificationList: [this.data.notificationList ?? []],
        jobs: [this.data.jobs ?? []],
        active:[this.data.active]
    });

    // Schedule editor state
    scheduleFrequency = signal<'daily' | 'weekly'>('daily');
    scheduleTime = signal('');
    scheduleDays = signal<string[]>([]);

    readonly dayOptions = [
        {value: 'MON', label: 'Monday'},
        {value: 'TUE', label: 'Tuesday'},
        {value: 'WED', label: 'Wednesday'},
        {value: 'THU', label: 'Thursday'},
        {value: 'FRI', label: 'Friday'},
        {value: 'SAT', label: 'Saturday'},
        {value: 'SUN', label: 'Sunday'},
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

        this.form.patchValue({cronExpression: cron});
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
            lastRunTime: this.data.lastTimeRun,
            cronExpression: this.data.cronExpression ?? '',
            notificationList: this.data.notificationList || [],
            jobs: this.data.jobs ?? []
        });

        // Sync existing cron string into the editor
        this.parseCronToEditor(this.data.cronExpression ?? '');

        // Populate existing jobs and emails from batch data
        this.currentBatchTests.set(
            this.data.jobs
        );
        this.emailList.set(this.data.notificationList ?? []);

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

        this.jobsApi.getAllJobs().subscribe({
            next: (response) => {
                this.availableTests.set(response.body ?? []);
                this.loading.set(false);
            },
            error: () => {
                this.loading.set(false);
            }
        });
    }


    addEmail(): void {
        const email = this.emailInputControl.value?.trim() ?? '';
        if (!email) return;
        if (!this.emailList().includes(email)) {
            this.emailList.update(list => [...list, email]);
        }
        this.emailInputControl.setValue('');
    }

    removeEmail(email: string): void {
        this.emailList.update(list => list.filter(e => e !== email));
    }


    /* Jobs */

    // Remove a single test from the batch
    removeTest(test: TestItem): void {
        this.currentBatchTests.update(tests => tests.filter(t => t.jobId !== test.jobId));
    }

    // Remove all tests from the batch
    clearAllTests(): void {
        this.currentBatchTests.set([]);
    }

    // Add a test to the batch
    addTest(test: TestItem): void {
        if (!this.currentBatchTests().some(t => t.jobId === test.jobId)) {
            this.currentBatchTests.update(tests => [...tests, test]);
        }
    }


    save() {
        this.form.markAllAsTouched();
        if (this.form.invalid) {
            return;
        }
        const newBatch = {
            ...this.form.value,
            jobs: this.currentBatchTests(),
            emails: this.emailList(),
            active: false,
            isNew: this.isNew
        };
        return this.batchService.saveBatch(newBatch).subscribe({
            next: (response) => {
                this.isNew = false;
                this.form.patchValue({
                    id: response.body?.id,
                    batchName: response.body?.batchName,
                    startTime: response.body?.startTime,
                    lastRunTime: response.body?.lastTimeRun,
                    cronExpression: response.body?.cronExpression ?? '',
                    notificationList: response.body?.notificationList || [],
                    jobs: response.body?.jobs,
                    active: response.body?.active
                });
                this.dialogRef.close(response.body);
            },
            error: (error) => console.error('Error:', error)
        });
    }

    private getFieldLabel(fieldName: string): string {
        const labels: { [key: string]: string } = {
            'batchName': 'Batch name',
        };
        return labels[fieldName] || fieldName;
    }

    getErrorMessage(fieldName: string): string {
        const control = this.form.get(fieldName);
        if (!control?.errors || !control.touched) {
            return '';
        }

        if (control.errors['required']) {
            return `${this.getFieldLabel(fieldName)} is required`;
        }

        if (control.errors['minlength']) {
            return `Minimum length is ${control.errors['minlength'].requiredLength}`;
        }

        if (control.errors['maxlength']) {
            return `Maximum length is ${control.errors['maxlength'].requiredLength} characters`;
        }

        if (control.errors['pattern']) {
            return 'Only letters, numbers, hyphens, and underscores are allowed';
        }

        return 'Invalid input';
    }



}

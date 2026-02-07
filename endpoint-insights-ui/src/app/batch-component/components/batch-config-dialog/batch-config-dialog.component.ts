import {Component, OnInit, inject, signal, ViewChild} from '@angular/core';
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
import { MatListOption, MatSelectionList} from "@angular/material/list";
import {BatchService} from "../../../services/batch.service";
import {debounceTime, distinctUntilChanged, Observable, switchMap} from "rxjs";
import {UserService} from "../../../services/user.service";


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
    loading = signal(false);


    form = this.fb.group({
        id: [this.data.id],
        batchName: [this.data.batchName ?? '', [Validators.required, Validators.maxLength(120)]],
        startTime: [this.data.startTime ?? ''],
        lastRunTime: [this.data.lastRunTime ?? ''],
        scheduledDays: [this.data.scheduledDays ?? ''],
        nextRunTime: [this.data.nextRunTime ?? ''],
        nextRunDate: [this.data.nextRunDate ?? ''],
        notificationList: [this.data.notificationList ?? []]

    });

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
            notificationList: this.data.notificationList || []
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

    saveTitle(): void {
        // Hook for future PATCH /batches/:id {title}
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
                    scheduledDays: response.scheduledDays,
                    nextRunTime: response.nextRunTime,
                    nextRunDate: response.nextRunDate,
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

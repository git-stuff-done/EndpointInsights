import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { debounceTime, forkJoin, Subject, takeUntil } from 'rxjs';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatBadgeModule } from '@angular/material/badge';
import { MatDialog } from '@angular/material/dialog';
import { CreateJobModal } from '../../components/create-job-modal/create-job-modal';
import { EditJobModal } from '../../components/edit-job-modal/edit-job-modal';
import { TestItem } from '../../models/test.model';
import { JOB_STATUSES, JobStatus } from '../../common/job.constants';
import { JobService } from '../../services/job-services';
import { TestRunService } from '../../services/test-run.service';
import { ToastService } from '../../services/toast.service';
import { BatchService } from '../../services/batch.service';

@Component({
  selector: 'app-test-overview',
  standalone: true,
  templateUrl: './test-overview.html',
  styleUrl: './test-overview.scss',
  imports: [
      CommonModule,
      MatIconModule,
      MatButtonModule,
      MatMenuModule,
      MatBadgeModule,
      ReactiveFormsModule,
      MatFormFieldModule,
      MatInputModule,
  ],
})
export class TestOverview implements OnInit, OnDestroy {
    tests: TestItem[] = [];
    filteredTests: TestItem[] = [];
    searchControl = new FormControl('');
    selectedStatuses = new Set<JobStatus>();
    readonly JOB_STATUSES = JOB_STATUSES;
    private destroy$ = new Subject<void>();

    constructor(
        private dialog: MatDialog,
        private jobService: JobService,
        private testRunService: TestRunService,
        private toastService: ToastService,
        private batchService: BatchService,
    ) {}

    ngOnInit(): void {
        this.searchControl.valueChanges
            .pipe(debounceTime(200), takeUntil(this.destroy$))
            .subscribe(() => this.applyFilter());
        this.loadTests();
    }

    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
    }

    loadTests(): void {
        forkJoin({
            jobs: this.jobService.getAllJobs(),
            activity: this.testRunService.getRecentActivity(100),
            batches: this.batchService.getAllBatches(),
        }).subscribe({
            next: ({ jobs, activity, batches }) => {
                const statusMap = new Map<string, string>();
                for (const run of activity) {
                    if (run.jobId && !statusMap.has(run.jobId)) {
                        statusMap.set(run.jobId, run.status);
                    }
                }
                const batchMap = new Map<string, string[]>();
                for (const batch of (batches.body ?? [])) {
                    for (const job of (batch.jobs ?? [])) {
                        const jobId = (job as any).jobId ?? job.id;
                        const existing = batchMap.get(jobId) ?? [];
                        batchMap.set(jobId, [...existing, batch.batchName]);
                    }
                }
                this.tests = jobs.map((j: any) => ({
                    id: j.jobId,
                    name: j.name,
                    batch: (batchMap.get(j.jobId) ?? []).join(', '),
                    description: j.description ?? '',
                    gitUrl: j.gitUrl ?? '',
                    gitAuthType: j.gitAuthType,
                    gitUsername: j.gitUsername,
                    gitPassword: j.gitPassword,
                    gitSshPrivateKey: j.gitSshPrivateKey,
                    gitSshPassphrase: j.gitSshPassphrase,
                    runCommand: j.runCommand ?? '',
                    compileCommand: j.compileCommand ?? '',
                    jmeterTestName: j.jmeterTestName ?? '',
                    jobType: j.jobType ?? '',
                    createdAt: j.createdDate ?? '',
                    createdBy: j.createdBy ?? '',
                    status: this.mapStatus(statusMap.get(j.jobId)),
                    threshold: 20,

                }));
                this.applyFilter();
            },
            error: () => {
                this.toastService.onError('Failed to load tests.');
            }
        });
    }

    private mapStatus(activityStatus: string | undefined): JobStatus {
        switch (activityStatus) {
            case 'PASS':    return 'SUCCESS';
            case 'FAIL':    return 'FAILED';
            case 'RUNNING': return 'RUNNING';
            case 'PENDING': return 'PENDING';
            default:        return 'STOPPED';
        }
    }

    private applyFilter(): void {
        const term = (this.searchControl.value ?? '').trim().toLowerCase();
        this.filteredTests = this.tests.filter(t => {
            const matchesSearch = !term ||
                t.name.toLowerCase().includes(term) ||
                (t.description ?? '').toLowerCase().includes(term);
            const matchesStatus = this.selectedStatuses.size === 0 || this.selectedStatuses.has(t.status);
            return matchesSearch && matchesStatus;
        });
    }

    get hasActiveFilters(): boolean {
        return this.selectedStatuses.size > 0;
    }

    toggleStatus(s: JobStatus) {
        if (this.selectedStatuses.has(s)) {
            this.selectedStatuses.delete(s);
        } else {
            this.selectedStatuses.add(s);
        }
        this.applyFilter();
    }

    isStatusSelected(s: JobStatus): boolean {
        return this.selectedStatuses.has(s);
    }

    onFilter() {
        console.log('Filter Button clicked');
    }

    onOpen(t: TestItem) { console.log('Open Clicked') }

    onRun(t: TestItem) {
        this.jobService.runJob(t.id).subscribe({
            next: () => this.toastService.onSuccess('Test run started!'),
            error: () => this.toastService.onError('Failed to start test run.'),
        });
    }

    onEdit(t: TestItem) { this.openEditModal(t) }

    onDelete(t: TestItem) {
        this.jobService.deleteJob(t.id).subscribe({
            next: () => {
                this.toastService.onSuccess('Test deleted.');
                this.tests = this.tests.filter(item => item.id !== t.id);
                this.applyFilter();
            },
            error: () => this.toastService.onError('Failed to delete test.'),
        });
    }

    openCreateJobModal() {
        const dialogRef = this.dialog.open(CreateJobModal, {
            width: '600px',
            maxWidth: '95vw'
        });

        dialogRef.afterClosed().subscribe((result: any) => {
            if (result) {
                this.loadTests();
            }
        });
    }

    openEditModal(t: TestItem) {
        const dialogRef = this.dialog.open(EditJobModal, {
            width: '600px',
            maxWidth: '95vw',
            data: t,
        });
        dialogRef.afterClosed().subscribe((result: any) => {
            if (result) {
                this.loadTests();
            }
        });
    }
}

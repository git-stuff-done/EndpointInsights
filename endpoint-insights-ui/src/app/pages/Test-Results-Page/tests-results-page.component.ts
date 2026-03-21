import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { debounceTime, Subject, takeUntil } from 'rxjs';
import { RecentActivity } from '../../models/test-run.model';
import { TestRunService } from '../../services/test-run.service';

@Component({
    selector: 'app-tests-results-page',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatTableModule,
        MatSortModule,
        MatFormFieldModule,
        MatInputModule,
        MatIconModule,
        MatButtonModule,
        MatProgressSpinnerModule,
    ],
    templateUrl: './tests-results-page.component.html',
    styleUrl: './tests-results-page.component.scss',
})
export class TestsResultsPageComponent implements OnInit, OnDestroy {
    @ViewChild(MatSort) sort!: MatSort;

    dataSource = new MatTableDataSource<RecentActivity>([]);
    displayedColumns = ['testName', 'runId', 'jobId', 'dateRun', 'durationMs', 'startedBy', 'status', 'actions'];
    searchControl = new FormControl('');
    isLoading = true;
    loadError: string | null = null;

    private destroy$ = new Subject<void>();

    constructor(
        private testRunService: TestRunService,
        private route: ActivatedRoute,
        private router: Router,
    ) {}

    ngOnInit(): void {
        this.dataSource.filterPredicate = (row: RecentActivity, filter: string) => {
            const term = filter.trim().toLowerCase();
            return (
                row.testName.toLowerCase().includes(term) ||
                row.runId.toLowerCase().includes(term) ||
                (row.jobId ?? '').toLowerCase().includes(term)
            );
        };

        const params = this.route.snapshot.queryParamMap;
        const preselected = params.get('runId') ?? params.get('uuid') ?? params.get('name') ?? '';
        if (preselected) {
            this.searchControl.setValue(preselected);
            this.dataSource.filter = preselected.trim().toLowerCase();
        }

        this.searchControl.valueChanges
            .pipe(debounceTime(200), takeUntil(this.destroy$))
            .subscribe(value => {
                this.dataSource.filter = (value ?? '').trim().toLowerCase();
            });

        this.testRunService.getRecentActivity(100).subscribe({
            next: (activity: RecentActivity[]) => {
                this.dataSource.data = activity;
                this.dataSource.sort = this.sort;
                this.isLoading = false;
            },
            error: () => {
                this.loadError = 'Unable to load test results.';
                this.isLoading = false;
            },
        });
    }

    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
    }

    viewResult(row: RecentActivity): void {
        this.router.navigate(['/test-results/view'], { state: { runId: row.runId } });
    }

    statusClass(status: string): string {
        switch (status) {
            case 'PASS': return 'status-pass';
            case 'FAIL': return 'status-fail';
            case 'WARN': return 'status-warn';
            default: return 'status-unknown';
        }
    }
}

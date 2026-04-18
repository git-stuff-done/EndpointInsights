import {AfterViewInit, Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {DatePipe, DecimalPipe, NgClass, NgStyle} from '@angular/common';
import {FormControl, ReactiveFormsModule} from '@angular/forms';
import {ActivatedRoute, Router} from '@angular/router';
import {
    MatCell,
    MatCellDef,
    MatColumnDef,
    MatHeaderCell,
    MatHeaderCellDef,
    MatHeaderRow,
    MatHeaderRowDef,
    MatNoDataRow,
    MatRow,
    MatRowDef,
    MatTable,
    MatTableDataSource
} from '@angular/material/table';
import {MatSort} from '@angular/material/sort';
import {MatPaginator} from '@angular/material/paginator';
import {MatFormField, MatLabel, MatSuffix} from '@angular/material/form-field';
import {MatInput} from '@angular/material/input';
import {MatIcon} from '@angular/material/icon';
import {MatIconButton} from '@angular/material/button';
import {MatProgressSpinner} from '@angular/material/progress-spinner';
import {MatTooltip} from '@angular/material/tooltip';
import {debounceTime, Subject, takeUntil} from 'rxjs';
import {RecentActivity} from '../../models/test-run.model';
import {TestRunService} from '../../services/test-run.service';
import {MatDatepicker, MatDatepickerInput, MatDatepickerToggle} from "@angular/material/datepicker";
import {provideNativeDateAdapter} from "@angular/material/core";
import {PerformanceChart} from "../../performance-chart/performance-chart";
import {NotificationService} from "../../services/notification.service";

@Component({
    selector: 'app-tests-results-page',
    standalone: true,
    imports: [
        MatDatepicker,
        MatDatepickerToggle,
        MatDatepickerInput,
        MatSuffix,
        PerformanceChart,
        MatPaginator,
        MatIcon,
        MatProgressSpinner,
        MatTable,
        MatTooltip,
        MatCell,
        MatHeaderCell,
        MatColumnDef,
        MatHeaderCellDef,
        MatCellDef,
        NgClass,
        MatHeaderRow,
        MatRow,
        MatLabel,
        MatFormField,
        NgStyle,
        ReactiveFormsModule,
        MatHeaderRowDef,
        MatRowDef,
        MatNoDataRow,
        MatInput,
        MatIconButton,
        MatSort,
        DecimalPipe,
        DatePipe

    ],
    providers: [
        provideNativeDateAdapter()
    ],
    templateUrl: './tests-results-page.component.html',
    styleUrl: './tests-results-page.component.scss',
})
export class TestsResultsPageComponent implements OnInit, AfterViewInit, OnDestroy {
    @ViewChild(MatSort) sort!: MatSort;
    @ViewChild(MatPaginator) paginator!: MatPaginator;

    dataSource = new MatTableDataSource<RecentActivity>([]);
    displayedColumns = ['batchName', 'testName', 'runId', 'dateRun', 'durationMs', 'startedBy', 'status', 'actions'];
    searchControl = new FormControl('');
    purgeBeforeDate = new FormControl<Date | null>(null);
    purgeBeforeTime = new FormControl<string>('00:00');
    isLoading = true;
    loadError: string | null = null;

    protected chartBatchId: string = '';
    protected chartJobId: string = '';
    protected displayChart: boolean = false;

    public maxDate = new Date();

    private destroy$ = new Subject<void>();

    constructor(
        private testRunService: TestRunService,
        private route: ActivatedRoute,
        private router: Router,
        private notificationService: NotificationService
    ) {}

    ngOnInit(): void {
        const state = window.history.state;
        if (state) {
            this.displayChart = state.displayGraph ?? false;

            this.chartBatchId = state.batchId ?? null;
            this.chartJobId = state.jobId ?? null;
        }

        this.dataSource.filterPredicate = (row: RecentActivity, filter: string) => {
            const term = filter.trim().toLowerCase();
            return (
                row.testName?.toLowerCase().includes(term) ||
                row.batchName?.toLowerCase().includes(term) ||
                row.runId.toLowerCase().includes(term) ||
                row.batchId?.toLowerCase().includes(term) ||
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
                this.displayChart = false;
                this.dataSource.filter = (value ?? '').trim().toLowerCase();
            });

        this.loadResults();
    }

    ngAfterViewInit(): void {
        this.dataSource.sort = this.sort;
        this.dataSource.paginator = this.paginator;

        this.dataSource.sortingDataAccessor = (item: RecentActivity, property: string) => {
            switch (property) {
                case 'dateRun':
                    return item.dateRun ? new Date(item.dateRun).getTime() : 0;
                case 'durationMs':
                    return item.durationMs || 0;
                default:
                    return (item as any)[property];
            }
        };

    }

    ngOnDestroy(): void {
        this.destroy$.next();
        this.destroy$.complete();
    }

    loadResults(): void {
        this.testRunService.getRecentActivity(100).subscribe({
            next: (activity: RecentActivity[]) => {
                this.dataSource.data = activity.sort((a, b) => {
                    const dateA = a.dateRun ? new Date(a.dateRun).getTime() : 0;
                    const dateB = b.dateRun ? new Date(b.dateRun).getTime() : 0;
                    return dateB - dateA;
                });
                this.isLoading = false;

                setTimeout(() => {
                    if (this.sort) {
                        this.dataSource.sort = this.sort;
                        this.sort.active = 'dateRun';
                        this.sort.direction = 'desc';
                    }
                    this.dataSource.paginator = this.paginator;
                });
            },
            error: () => {
                this.loadError = 'Unable to load test results.';
                this.isLoading = false;
            },
        });
    }

    viewResult(row: RecentActivity): void {
        this.router.navigate(['/test-results/view'], { state: { runId: row.runId } });
    }

    purge(): void {
        const date = this.purgeBeforeDate.value;
        if (!date) return;
        const [hours, minutes] = (this.purgeBeforeTime.value ?? '00:00').split(':').map(Number);
        const datetime = new Date(date);
        datetime.setHours(hours, minutes, 0, 0);

        this.testRunService.deleteBefore(datetime).subscribe({
            next: res => {
                const deletedRuns = res.body.deletedRuns;
                this.notificationService.showToast(`Successfully purged ${deletedRuns} test run${deletedRuns !== 1 ? 's' : ''}`, 'success');
                this.loadResults();
            },
            error: err => {
                this.notificationService.showToast('Error purging test runs', 'error');
            }
        });
    }

    statusClass(status: string): string {
        switch (status) {
            case 'PASS': return 'status-pass';
            case 'FAIL': return 'status-fail';
            case 'WARN': return 'status-warn';
            default: return 'status-unknown';
        }
    }

    protected readonly console = console;
}
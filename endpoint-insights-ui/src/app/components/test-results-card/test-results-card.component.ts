import { ChangeDetectionStrategy, ChangeDetectorRef, Component, EventEmitter, Input, Output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TestRecord, TestStatus } from '../../models/test-record.model';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { LogsDialogComponent } from '../logs-dialog/logs-dialog.component';

// Angular Material
import { MatCardModule } from '@angular/material/card';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatChipsModule } from '@angular/material/chips';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';

@Component({
  selector: 'app-test-results-card',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatExpansionModule,
    MatChipsModule,
    MatTableModule,
    MatButtonModule,
    MatDividerModule,
    MatIconModule,
    MatTooltipModule,
    MatIconModule,
    MatDialogModule,
  ],
  templateUrl: './test-results-card.component.html',
  styleUrls: ['./test-results-card.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TestResultsCardComponent {

  constructor(private cdr: ChangeDetectorRef, private dialog: MatDialog) {}

  @Input({ required: true }) test!: TestRecord;
  @Input() defaultExpanded = false;

  @Output() toggled = new EventEmitter<boolean>();
  expanded = signal(false);

  logsLoading = false;
  logsError: string | null = null;
  logsText = '';
  fullLogsText = '';

  ngOnInit() { this.expanded.set(this.defaultExpanded); }
  onOpened() {
      this.expanded.set(true);
      this.toggled.emit(true);

      // load logs the first time we expand
      if (!this.logsText && !this.logsLoading) {
          this.loadLogsPreview();
      }
    }
  onClosed() { this.expanded.set(false); this.toggled.emit(false); }

  statusClass(s: TestStatus) {
    return ({ PASS: 'pass', WARN: 'warn', FAIL: 'fail', UNKNOWN: 'unknown' } as const)[s] ?? 'unknown';
  }

    chipClass(status: string): string {
        switch ((status || '').toLowerCase()) {
            case 'pass': return 'chip-pass';
            case 'warn': return 'chip-warn';
            case 'fail': return 'chip-fail';
            default:     return 'chip-unknown';
        }
    }


  num(n?: number, suffix = '') { return (n ?? n === 0) ? `${n}${suffix}` : '—'; }
  pct(n?: number) { return (typeof n === 'number') ? `${n.toFixed(1)}%` : '—'; }
  lastRun() {
    if (!this.test?.lastRunIso) return '—';
    const d = new Date(this.test.lastRunIso);
    return isNaN(d.valueOf()) ? '—' : d.toLocaleString();
  }

  // Strict-template-safe formatters
  formatLatencyWF(): string {
    const cfg = this.test?.thresholds?.latencyMs;
    return cfg ? `${cfg.warn} / ${cfg.fail} ms` : '—';
  }
  formatErrorPctWF(): string {
    const cfg = this.test?.thresholds?.errorRatePct;
    return cfg ? `${cfg.warn}% / ${cfg.fail}%` : '—';
  }
  formatVolumeWF(): string {
    const cfg = this.test?.thresholds?.volumePerMin;
    return cfg ? `${cfg.warn} / ${cfg.fail}` : '—';
  }

  httpColumns = ['code', 'count'];

/* Log Code */
  private loadLogsPreview() {
    this.logsLoading = true;
    this.logsError = null;
    this.cdr.markForCheck();

    // Offline/mock for now (replace with service call later)
    setTimeout(() => {
      const lines: string[] = [];

          const startedIso = new Date().toISOString();
          lines.push(`=== Test Run: ${this.test.name} ===`);
          lines.push(`Started at: ${startedIso}`);
          lines.push('----------------------------------------');

          const MAX_LINES = 120; // enough to show scroll, still fast

          for (let i = 1; i <= MAX_LINES; i++) {
            // deterministic "random-ish" latency (faster than Math.random)
            const latency = 40 + ((i * 37) % 220);
            const level = latency > 200 ? 'WARN' : 'INFO';

            // don't allocate a new Date every line
            lines.push(
              `[${startedIso}] ${level} Request #${i} GET /api/resource/${i} -> 200 (${latency}ms)`
            );

            if (i % 30 === 0) {
              lines.push(
                `[${startedIso}] ERROR Assertion failed: expected latency < 150ms (observed ${latency}ms)`
              );
            }
          }

          lines.push('----------------------------------------');
          lines.push('[INFO] Aggregating metrics...');
          lines.push(`[INFO] P50: ${this.test.latencyMsP50 ?? '—'}ms`);
          lines.push(`[INFO] P95: ${this.test.latencyMsP95 ?? '—'}ms`);
          lines.push(`[INFO] P99: ${this.test.latencyMsP99 ?? '—'}ms`);
          lines.push('[INFO] Run completed');

          this.setLogsFromLines(lines);
          this.logsLoading = false;
          this.cdr.markForCheck();
        }, 0);
  }

    private setLogsFromLines(lines: string[]) {
      const width = String(lines.length).length;

      const numbered = lines.map((l, idx) =>
        `${String(idx + 1).padStart(width, ' ')}  ${l}`
      );

      // Store full logs
      this.fullLogsText = numbered.join('\n');

      // Store only first 80 lines for preview
      const PREVIEW_LINES = 80;
      this.logsText = numbered.slice(0, PREVIEW_LINES).join('\n');
    }

  openLogsModal() {
    this.dialog.open(LogsDialogComponent, {
      width: 'min(1100px, 95vw)',
      maxWidth: '95vw',
      data: {
        title: `Logs — ${this.test.name}`,
        logsText: this.fullLogsText || '(No logs available)',
        fileName: `logs-${this.test.id}.txt`,
      },
    });
  }
}

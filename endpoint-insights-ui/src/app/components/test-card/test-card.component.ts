import { ChangeDetectionStrategy, Component, EventEmitter, Input, Output, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TestRecord, TestStatus } from '../../models/test-record.model';

// Angular Material
import { MatCardModule } from '@angular/material/card';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatChipsModule } from '@angular/material/chips';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatDividerModule } from '@angular/material/divider';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-test-card',
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
  ],
  templateUrl: './test-card.component.html',
  styleUrls: ['./test-card.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TestCardComponent {
  @Input({ required: true }) test!: TestRecord;
  @Input() defaultExpanded = false;

  @Output() toggled = new EventEmitter<boolean>();
  expanded = signal(false);

  ngOnInit() { this.expanded.set(this.defaultExpanded); }
  onOpened() { this.expanded.set(true); this.toggled.emit(true); }
  onClosed() { this.expanded.set(false); this.toggled.emit(false); }

  statusClass(s: TestStatus) {
    return ({ PASS: 'pass', WARN: 'warn', FAIL: 'fail', UNKNOWN: 'unknown' } as const)[s] ?? 'unknown';
  }

  chipClass(s: TestStatus): string {
    return ({ PASS: 'chip-pass', WARN: 'chip-warn', FAIL: 'chip-fail', UNKNOWN: 'chip-unknown' } as const)[s] ?? 'chip-unknown';
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
}

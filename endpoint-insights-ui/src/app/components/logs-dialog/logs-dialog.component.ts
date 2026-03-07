import { ChangeDetectionStrategy, Component, Inject, computed, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ClipboardModule, Clipboard } from '@angular/cdk/clipboard';

export type LogsDialogData = {
  title: string;
  logsText: string; // already includes line numbers (your logsText)
  fileName?: string;
};

@Component({
  selector: 'app-logs-dialog',
  standalone: true,
  imports: [
    CommonModule,
    MatDialogModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    ClipboardModule,
  ],
  templateUrl: './logs-dialog.component.html',
  styleUrls: ['./logs-dialog.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LogsDialogComponent {
  query = signal('');
  lines = computed(() => this.filteredText().split('\n'));

  // Optional: filter lines when searching (fast + simple)
  filteredText = computed(() => {
    const q = this.query().trim().toLowerCase();
    if (!q) return this.data.logsText;

    return this.data.logsText
      .split('\n')
      .filter(line => line.toLowerCase().includes(q))
      .join('\n') || '(No matches)';
  });

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: LogsDialogData,
    private dialogRef: MatDialogRef<LogsDialogComponent>,
    private clipboard: Clipboard
  ) {}

  close() {
    this.dialogRef.close();
  }

  copyAll() {
    this.clipboard.copy(this.data.logsText);
  }

  download() {
    const name = this.data.fileName ?? 'logs.txt';
    const blob = new Blob([this.data.logsText], { type: 'text/plain;charset=utf-8' });
    const url = URL.createObjectURL(blob);

    const a = document.createElement('a');
    a.href = url;
    a.download = name;
    a.click();

    URL.revokeObjectURL(url);
  }

  getLogLevel(line: string): 'info' | 'warn' | 'error' | 'none' {
    if (line.includes('ERROR')) return 'error';
    if (line.includes('WARN')) return 'warn';
    if (line.includes('INFO')) return 'info';
    return 'none';
  }
}
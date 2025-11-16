import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { Batch } from '../models/batch.model';

const STORAGE_KEY = 'vsp.batches';

@Injectable({ providedIn: 'root' })
export class BatchStore {
    private readonly _batches$ = new BehaviorSubject<Batch[]>(this.load());

    readonly batches$ = this._batches$.asObservable();

    get batches(): Batch[] { return this._batches$.value; }

    /** Initialize with defaults if none in storage */
    private load(): Batch[] {
        const raw = localStorage.getItem(STORAGE_KEY);
        if (raw) {
            try { return JSON.parse(raw) as Batch[]; } catch {}
        }
        // Seed defaults once:
        const seed: Batch[] = [
            { id: 'B-2025-00123', title: 'Nightly ETL (US-East)', date: '2025-10-17T02:13:00Z' },
            { id: 'B-2025-00124', title: 'Customer Backfill – Oct', date: '2025-10-18T15:45:00Z' },
        ];
        localStorage.setItem(STORAGE_KEY, JSON.stringify(seed));
        return seed;
    }

    private save(next: Batch[]) {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(next));
        this._batches$.next(next);
    }

    setAll(next: Batch[]) { this.save(next); }

    /** Update a single batch by id with partial fields */
    update(id: string, patch: Partial<Batch>) {
        const next = this.batches.map(b => b.id === id ? { ...b, ...patch } : b);
        this.save(next);
    }
}

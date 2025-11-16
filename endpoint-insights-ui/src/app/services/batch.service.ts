import { Injectable } from '@angular/core';
import { Observable, of, delay } from 'rxjs';

export interface BatchMeta {
    id: string;
    name: string;
    testCount: number;
    nextRunIso?: string; // ISO 8601 datetime
}

@Injectable({ providedIn: 'root' })
export class BatchService {
    /** Simulated GET: fetch meta for a batch */
    getBatchMeta(batchId: string): Observable<BatchMeta> {
        // TODO: replace with real HTTP call
        return of({
            id: batchId,
            name: `Batch ${batchId}`,
            testCount: 12,
            nextRunIso: new Date(Date.now() + 36e5).toISOString(), // +1 hr
        }).pipe(delay(200));
    }

    /** Simulated PATCH/PUT: update name; return updated meta */
    updateBatchName(batchId: string, name: string): Observable<BatchMeta> {
        // TODO: replace with real HTTP call
        return of({
            id: batchId,
            name,
            testCount: 12,
            nextRunIso: new Date(Date.now() + 36e5).toISOString(),
        }).pipe(delay(200));
    }

    /** Simulated GET: just the count, if you want a quick refresh */
    getBatchTestCount(batchId: string): Observable<number> {
        return of(12).pipe(delay(150));
    }
}

import {inject, Inject, Injectable} from '@angular/core';
import { Observable, of, delay } from 'rxjs';
import {HttpClient, HttpResponse} from "@angular/common/http";
import {Batch} from "../models/batch.model";
import {BatchApi} from "../batch-component/api/batch-api";

export interface BatchMeta {
    id: string;
    name: string;
    testCount: number;
    nextRunIso?: string; // ISO 8601 datetime
}

@Injectable({ providedIn: 'root' })
export class BatchService {

    private batchApi = inject(BatchApi);

    /** Simulated GET: fetch meta for a batch */
    getAllBatches(): Observable<Batch[]> {
        return this.batchApi.getAllBatches();
    }

    saveBatch(form: any):Observable<HttpResponse<Batch>>{
        return this.batchApi.saveBatch(form)
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

}

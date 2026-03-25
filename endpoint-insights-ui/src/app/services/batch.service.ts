import {inject, Injectable} from '@angular/core';
import {delay, Observable, of} from 'rxjs';
import {HttpResponse} from "@angular/common/http";
import {Batch} from "../models/batch.model";
import {BatchApi} from "../batch-component/api/batch-api";
import {TestRun} from "../models/test-run.model";

export interface BatchMeta {
    id: string;
    name: string;
    testCount: number;
    nextRunIso?: string; // ISO 8601 datetime
}

@Injectable({ providedIn: 'root' })
export class BatchService {

    private batchApi = inject(BatchApi);

    getAllBatches(): Observable<HttpResponse<Batch[]>> {
        return this.batchApi.getAllBatches();
    }

    saveBatch(form: any):Observable<HttpResponse<Batch>>{

        return this.batchApi.saveBatch(form)
    }

    deleteBatch(batch:Batch):Observable<HttpResponse<Batch>>{
        return this.batchApi.deleteBatch(batch.id)
    }

    runBatch(batch: Batch):Observable<HttpResponse<TestRun>> {
        return this.batchApi.runBatch(batch.id);
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

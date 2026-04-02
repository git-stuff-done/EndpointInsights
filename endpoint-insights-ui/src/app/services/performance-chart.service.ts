import { Injectable } from '@angular/core';
import { HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ChartPoint, ChartResponse, ChartSeries } from '../models/chart.model';
import { map } from 'rxjs/operators';
import { environment } from '../../environment';
import { HttpInterceptorService } from './http-interceptor.service';


@Injectable({
  providedIn: 'root',
})
export class PerformanceChartService {
  private readonly apiUrl = `${environment.apiUrl}/dashboard/charts/performance`;

  constructor(private http: HttpInterceptorService) {}

  getApiPerformanceChart(
    jobId?: string,
    batchId?: string,
    limit = 10
  ): Observable<ChartResponse> {
    if (jobId && batchId) {
      batchId = undefined;
      // throw new Error('Provide only one of jobId or batchId');
    }

    let params = new HttpParams().set('limit', limit.toString());

    if (jobId) {
      params = params.set('jobId', jobId);
    } else if (batchId) {
      params = params.set('batchId', batchId);
    }

    return this.http
      .get<ChartResponse>(`${this.apiUrl}?${params.toString()}`)
      .pipe(
        map(response => response.body ?? {
          title: '',
          xAxis: '',
          series: []
        })
      );
  }
}
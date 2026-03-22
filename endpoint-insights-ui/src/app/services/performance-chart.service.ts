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

  getApiPerformanceChart(jobId: string, limit = 10): Observable<ChartResponse> {
    const params = new HttpParams().set('limit', limit.toString());

    return this.http
      .get<ChartResponse>(
        `${this.apiUrl}/${jobId}`
      )
      .pipe(
        map(response => response.body ?? {
          title: '',
          xAxis: '',
          series: []
        })
      );
  }
}
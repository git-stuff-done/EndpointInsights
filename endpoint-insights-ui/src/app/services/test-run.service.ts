import {Injectable} from '@angular/core';
import {HttpParams} from '@angular/common/http';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {RecentActivity, TestRun} from '../models/test-run.model';
import {environment} from '../../environment';
import {HttpInterceptorService} from './http-interceptor.service';

@Injectable({
  providedIn: 'root',
})
export class TestRunService {
  private readonly apiUrl = `${environment.apiUrl}/test-runs`;

  constructor(private http: HttpInterceptorService) {}

  getRecentTestRuns(limit = 10): Observable<TestRun[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<TestRun[]>(`${this.apiUrl}/recent?${params.toString()}`).pipe(
      map(response => response.body ?? [])
    );
  }

  getRun(id: string): Observable<TestRun> {
    return this.http.get<TestRun>(`${this.apiUrl}/${id}`).pipe(
      map(response => response.body ?? null as any)
    );
  }

  getRecentActivity(limit = 10): Observable<RecentActivity[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<RecentActivity[]>(`${this.apiUrl}/recent-activity?${params.toString()}`).pipe(
      map(response => response.body ?? [])
    );
  }

  deleteRun(id: string): Observable<any> {
    return this.http.delete(`${this.apiUrl}/${id}`);
  }

  deleteBefore(date: Date): Observable<any> {
    return this.http.delete(`${this.apiUrl}?purgeDate=${date.toISOString()}`);
  }
}

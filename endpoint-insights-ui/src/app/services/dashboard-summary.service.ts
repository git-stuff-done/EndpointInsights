import { Injectable } from '@angular/core';
import { Observable, switchMap } from 'rxjs';
import { map, filter } from 'rxjs/operators';  // was missing
import { HttpInterceptorService } from './http-interceptor.service';
import { TestRunService } from './test-run.service';
import { DashboardSummaryResponse } from '../models/dashboard-summary.model';
import { environment } from '../../environment';
import { RecentActivity } from '../models/test-run.model';

@Injectable({ providedIn: 'root' })
export class DashboardSummaryService {
  private readonly apiUrl = `${environment.apiUrl}/dashboard`;

  constructor(
    private http: HttpInterceptorService,
    private testRunService: TestRunService
  ) {}

  getSummary(tests: RecentActivity[]): Observable<DashboardSummaryResponse> {
    return this.http.post<DashboardSummaryResponse>(
      `${this.apiUrl}/summary`,
      tests
    ).pipe(
      map(response => response.body),
      filter((body): body is DashboardSummaryResponse => body !== null)
    );
  }

  loadDashboardSummary(limit = 10): Observable<DashboardSummaryResponse> {
    return this.testRunService.getRecentActivity(limit).pipe(
      switchMap(activity => this.getSummary(activity))
    );
  }
}
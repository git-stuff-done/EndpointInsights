import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { TestRun } from '../models/test-run.model';
import { environment } from '../../environment';

@Injectable({
  providedIn: 'root',
})
export class TestRunService {
  private readonly apiUrl = `${environment.apiUrl}/test-runs`;

  constructor(private http: HttpClient) {}

  getRecentTestRuns(limit = 10): Observable<TestRun[]> {
    const params = new HttpParams().set('limit', limit.toString());
    return this.http.get<TestRun[]>(`${this.apiUrl}/recent`, { params });
  }
}

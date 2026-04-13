import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';

import { TestRunService } from './test-run.service';

describe('TestRunService', () => {
  let service: TestRunService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
    });
    service = TestBed.inject(TestRunService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('requests recent runs with limit param', () => {
    service.getRecentTestRuns(5).subscribe();

    const req = httpMock.expectOne('http://localhost:8080/api/test-runs/recent?limit=5');
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  describe('deleteRun()', () => {
    it('should send DELETE request to the correct URL', () => {
      const runId = 'eda90106-635f-44c0-acff-b45618a91433';
      service.deleteRun(runId).subscribe();

      const req = httpMock.expectOne(`http://localhost:8080/api/test-runs/${runId}`);
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });

    it('should return the response body', () => {
      const runId = 'eda90106-635f-44c0-acff-b45618a91433';
      let result: any;
      service.deleteRun(runId).subscribe(res => result = res);

      const req = httpMock.expectOne(`http://localhost:8080/api/test-runs/${runId}`);
      req.flush({ deleted: true });

      expect(result.body).toEqual({ deleted: true });
    });
  });

  describe('deleteBefore()', () => {
    it('should send DELETE request with purgeDate query param', () => {
      const date = new Date('2026-01-15T00:00:00.000Z');
      service.deleteBefore(date).subscribe();

      const req = httpMock.expectOne(
        `http://localhost:8080/api/test-runs?purgeDate=${date.toISOString()}`
      );
      expect(req.request.method).toBe('DELETE');
      req.flush(null);
    });

    it('should return the response body', () => {
      const date = new Date('2026-01-15T00:00:00.000Z');
      let result: any;
      service.deleteBefore(date).subscribe(res => result = res);

      const req = httpMock.expectOne(
        `http://localhost:8080/api/test-runs?purgeDate=${date.toISOString()}`
      );
      req.flush({ purged: 42 });

      expect(result.body).toEqual({ purged: 42 });
    });
  });
});

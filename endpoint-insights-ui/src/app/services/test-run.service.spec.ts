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
});

import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { of } from 'rxjs';
import { JobService } from './job-services';
import { TestItem } from '../models/test.model';
import { environment } from '../../environment';
import { AuthenticationService } from './authentication.service';

const BASE = `${environment.apiUrl}/jobs`;

const mockAuthService = {
    authState$: of(null),
    getToken: () => null,
};

describe('JobService', () => {
    let service: JobService;
    let httpMock: HttpTestingController;

    const testItem: TestItem = {
        id: '1',
        name: 'Test Job',
        batch: 'Batch A',
        description: 'desc',
        gitUrl: 'https://github.com/user/repo.git',
        runCommand: 'npm run test',
        compileCommand: 'npm run build',
        jobType: 'jmeter',
        createdAt: new Date(),
        createdBy: 'user',
        status: 'RUNNING',
        threshold: 20,

    };

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [
                JobService,
                { provide: AuthenticationService, useValue: mockAuthService },
            ],
        });
        service = TestBed.inject(JobService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('getAllJobs should GET from api', () => {
        service.getAllJobs().subscribe(result => {
            expect(result).toEqual([]);
        });

        const req = httpMock.expectOne(BASE);
        expect(req.request.method).toBe('GET');
        req.flush([]);
    });

    it('createJob should POST to api', () => {
        service.createJob(testItem).subscribe((result) => {
            expect(result).toEqual(testItem);
        });

        const req = httpMock.expectOne(BASE);
        expect(req.request.method).toBe('POST');
        expect(req.request.body).toEqual(testItem);
        req.flush(testItem);
    });

    it('updateJob should PUT to api', () => {
        service.updateJob('123', testItem).subscribe((result) => {
            expect(result).toEqual(testItem);
        });

        const req = httpMock.expectOne(`${BASE}/123`);
        expect(req.request.method).toBe('PUT');
        expect(req.request.body).toEqual(testItem);
        req.flush(testItem);
    });

    it('deleteJob should DELETE to api', () => {
        service.deleteJob('123').subscribe(result => {
            expect(result).toBeNull();
        });

        const req = httpMock.expectOne(`${BASE}/123`);
        expect(req.request.method).toBe('DELETE');
        req.flush(null);
    });

    it('runJob should POST to run endpoint', () => {
        service.runJob('123').subscribe(result => {
            expect(result).toBeTruthy();
        });

        const req = httpMock.expectOne(`${BASE}/123/run`);
        expect(req.request.method).toBe('POST');
        req.flush({ runId: 'abc' });
    });
});

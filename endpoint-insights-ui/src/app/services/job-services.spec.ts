import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { JobService } from './job-services';
import { TestItem } from '../models/test.model';

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
        status: 'RUNNING'
    };

    beforeEach(() => {
        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [JobService]
        });
        service = TestBed.inject(JobService);
        httpMock = TestBed.inject(HttpTestingController);
    });

    afterEach(() => {
        httpMock.verify();
    });

    it('createJob should POST to api', () => {
        service.createJob(testItem).subscribe((result) => {
            expect(result).toEqual(testItem);
        });

        const req = httpMock.expectOne('http://localhost:8080/api/jobs');
        expect(req.request.method).toBe('POST');
        expect(req.request.body).toEqual(testItem);
        req.flush(testItem);
    });

    it('updateJob should PUT to api', () => {
        service.updateJob('123', testItem).subscribe((result) => {
            expect(result).toEqual(testItem);
        });

        const req = httpMock.expectOne('http://localhost:8080/api/jobs/123');
        expect(req.request.method).toBe('PUT');
        expect(req.request.body).toEqual(testItem);
        req.flush(testItem);
    });
});

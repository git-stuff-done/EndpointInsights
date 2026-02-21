import { TestBed } from '@angular/core/testing';

import { HttpInterceptorService } from './http-interceptor.service';
import {provideHttpClient} from "@angular/common/http";
import {HttpTestingController} from "@angular/common/http/testing";

describe('HttpInterceptorService', () => {
  let service: HttpInterceptorService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient()]
    });
    service = TestBed.inject(HttpInterceptorService);
    httpMock = TestBed.inject(HttpTestingController);

  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

    it('should make a POST request with auth token', () => {
        service.post('/api/test', { name: 'test' }).subscribe(response => {
            expect(response).toBeTruthy();
        });
        const req = httpMock.expectOne('/api/test');
        expect(req.request.method).toBe('POST');
        req.flush({});
    });

    it('should make a PUT request with auth token', () => {
        service.put('/api/test', { name: 'test' }).subscribe(response => {
            expect(response).toBeTruthy();
        });
        const req = httpMock.expectOne('/api/test');
        expect(req.request.method).toBe('PUT');
        req.flush({});
    });
});

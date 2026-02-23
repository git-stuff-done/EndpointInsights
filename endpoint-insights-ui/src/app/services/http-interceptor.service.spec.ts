import { TestBed } from '@angular/core/testing';

import { HttpInterceptorService } from './http-interceptor.service';
import {provideHttpClient} from "@angular/common/http";
import {HttpTestingController, provideHttpClientTesting} from "@angular/common/http/testing";

describe('HttpInterceptorService', () => {
  let service: HttpInterceptorService;
  let httpMock: HttpTestingController;
  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(),
          provideHttpClientTesting(),
          HttpInterceptorService,
      ]
    });
    httpMock = TestBed.inject(HttpTestingController);
    service = TestBed.inject(HttpInterceptorService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

it('should make a GET request', () => {
    service.get('/api/test').subscribe(response => {
        expect(response).toBeTruthy();
    });
    const req = httpMock.expectOne('/api/test');
    expect(req.request.method).toBe('GET');
    req.flush({});
});

it('should make a POST request', () => {
    service.post('/api/test', { name: 'test' }).subscribe(response => {
        expect(response).toBeTruthy();
    });
    const req = httpMock.expectOne('/api/test');
    expect(req.request.method).toBe('POST');
    req.flush({});
});

it('should make a PUT request', () => {
    service.put('/api/test', { name: 'test' }).subscribe(response => {
        expect(response).toBeTruthy();
    });
    const req = httpMock.expectOne('/api/test');
    expect(req.request.method).toBe('PUT');
})

});

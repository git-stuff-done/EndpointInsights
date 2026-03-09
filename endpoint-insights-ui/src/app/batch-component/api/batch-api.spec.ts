import { of, throwError } from 'rxjs';
import { HttpClient, HttpResponse } from '@angular/common/http';
import {BatchApi} from "./batch-api";
import {HttpInterceptorService} from "../../services/http-interceptor.service";
import {ToastService} from "../../services/toast.service";
import {Batch} from "../../models/batch.model";
import {TestBed} from "@angular/core/testing";
import {environment} from "../../../environment";


describe('BatchApi', () => {
    let service: BatchApi;
    let httpInterceptSpy: jasmine.SpyObj<HttpInterceptorService>;
    let toastSpy: jasmine.SpyObj<ToastService>;

    const mockBatch: Batch = {
        id: '123',
        batchName: 'Test Batch',
        active: false,
        startTime: '',
        lastRunTime: '',
        notificationList: [],
        jobs: [],
        isNew: false
    };

    beforeEach(() => {
        httpInterceptSpy = jasmine.createSpyObj('HttpInterceptorService', ['get', 'post', 'put', 'delete']);
        toastSpy = jasmine.createSpyObj('ToastService', ['onSuccess', 'onError']);

        TestBed.configureTestingModule({
            providers: [
                BatchApi,
                { provide: HttpInterceptorService, useValue: httpInterceptSpy },
                { provide: ToastService, useValue: toastSpy },
                { provide: HttpClient, useValue: jasmine.createSpyObj('HttpClient', ['get']) }
            ]
        });

        service = TestBed.inject(BatchApi);
    });


    it('should get all batches', () => {
        const mockResponse = new HttpResponse({ body: [mockBatch] });
        httpInterceptSpy.get.and.returnValue(of(mockResponse));

        service.getAllBatches().subscribe(response => {
            expect(response.body).toEqual([mockBatch]);
        });

        expect(httpInterceptSpy.get).toHaveBeenCalledWith(`${environment.apiUrl}/batches`);
    });


    it('should delete a batch by id', () => {
        const mockResponse = new HttpResponse({ body: mockBatch });
        httpInterceptSpy.delete.and.returnValue(of(mockResponse));

        service.deleteBatch('123').subscribe(response => {
            expect(response.body).toEqual(mockBatch);
        });

        expect(httpInterceptSpy.delete).toHaveBeenCalledWith(`${environment.apiUrl}/batches/123`);
    });


    it('should POST when batch isNew', () => {
        const newBatch = { ...mockBatch, isNew: true };
        const mockResponse = new HttpResponse({ body: newBatch });
        httpInterceptSpy.post.and.returnValue(of(mockResponse));

        service.saveBatch(newBatch).subscribe();

        expect(httpInterceptSpy.post).toHaveBeenCalledWith(
            `${environment.apiUrl}/batches`,
            newBatch
        );
    });

    it('should show success toast on POST success', () => {
        const newBatch = { ...mockBatch, isNew: true };
        httpInterceptSpy.post.and.returnValue(of(new HttpResponse({ body: newBatch })));

        service.saveBatch(newBatch).subscribe();

        expect(toastSpy.onSuccess).toHaveBeenCalledWith('Successfully saved batch item');
    });

    it('should show error toast and rethrow on POST failure', () => {
        const newBatch = { ...mockBatch, isNew: true };
        httpInterceptSpy.post.and.returnValue(throwError(() => new Error('error')));

        service.saveBatch(newBatch).subscribe({
            error: () => {
                expect(toastSpy.onError).toHaveBeenCalledWith('Unable to save batch item');
            }
        });
    });


    it('should PUT when batch is not new', () => {
        const mockResponse = new HttpResponse({ body: mockBatch });
        httpInterceptSpy.put.and.returnValue(of(mockResponse));

        service.saveBatch(mockBatch).subscribe();

        expect(httpInterceptSpy.put).toHaveBeenCalledWith(
            `${environment.apiUrl}/batches/123`,
            mockBatch
        );
    });

    it('should show success toast on PUT success', () => {
        httpInterceptSpy.put.and.returnValue(of(new HttpResponse({ body: mockBatch })));

        service.saveBatch(mockBatch).subscribe();

        expect(toastSpy.onSuccess).toHaveBeenCalledWith('Successfully saved batch item');
    });

    it('should show error toast and rethrow on PUT failure', () => {
        httpInterceptSpy.put.and.returnValue(throwError(() => new Error('error')));

        service.saveBatch(mockBatch).subscribe({
            error: () => {
                expect(toastSpy.onError).toHaveBeenCalledWith('Unable to save batch item');
            }
        });
    });
});
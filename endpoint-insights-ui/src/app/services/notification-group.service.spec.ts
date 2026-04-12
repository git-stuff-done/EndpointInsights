import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { NotificationGroupService } from './notification-group.service';
import { HttpInterceptorService } from './http-interceptor.service';
import { ToastService } from './toast.service';
import { environment } from '../../environment';
import { HttpResponse } from '@angular/common/http';
import { NotificationGroup } from '../models/notification-group.model';
import { of } from 'rxjs';

describe('NotificationGroupService', () => {
    let service: NotificationGroupService;
    let httpInterceptSpy: jasmine.SpyObj<HttpInterceptorService>;
    let toastServiceSpy: jasmine.SpyObj<ToastService>;

    const mockGroup: NotificationGroup = {
        id: '123-456',
        name: 'Test Group',
        description: 'Test Description',
        members: ['test1@example.com', 'test2@example.com']
    };

    beforeEach(() => {
        httpInterceptSpy = jasmine.createSpyObj('HttpInterceptorService', ['get', 'post', 'put', 'delete']);
        toastServiceSpy = jasmine.createSpyObj('ToastService', ['onSuccess', 'onError']);

        TestBed.configureTestingModule({
            imports: [HttpClientTestingModule],
            providers: [
                NotificationGroupService,
                { provide: HttpInterceptorService, useValue: httpInterceptSpy },
                { provide: ToastService, useValue: toastServiceSpy }
            ]
        });

        service = TestBed.inject(NotificationGroupService);
    });

    it('should be created', () => {
        expect(service).toBeTruthy();
    });

    it('should fetch all notification groups', () => {
        const mockResponse = new HttpResponse({ body: [mockGroup] });
        httpInterceptSpy.get.and.returnValue(of(mockResponse));

        service.getAllGroups().subscribe(response => {
            expect(response.body).toEqual([mockGroup]);
        });

        expect(httpInterceptSpy.get).toHaveBeenCalledWith(`${environment.apiUrl}/notification-groups`);
    });

    it('should create a new notification group', () => {
        const mockResponse = new HttpResponse({ body: mockGroup });
        httpInterceptSpy.post.and.returnValue(of(mockResponse));

        service.createGroup('New Group', 'New Description', ['new@example.com']).subscribe(response => {
            expect(response.body).toEqual(mockGroup);
        });

        expect(httpInterceptSpy.post).toHaveBeenCalled();
        expect(toastServiceSpy.onSuccess).toHaveBeenCalled();
    });

    it('should update an existing notification group', () => {
        const mockResponse = new HttpResponse({ body: mockGroup });
        httpInterceptSpy.put.and.returnValue(of(mockResponse));

        service.updateGroup('123-456', 'Updated Group', 'Updated Description').subscribe(response => {
            expect(response.body).toEqual(mockGroup);
        });

        expect(httpInterceptSpy.put).toHaveBeenCalled();
        expect(toastServiceSpy.onSuccess).toHaveBeenCalled();
    });

    it('should delete a notification group', () => {
        const mockResponse = new HttpResponse({ body: null });
        httpInterceptSpy.delete.and.returnValue(of(mockResponse));

        service.deleteGroup('123-456').subscribe();

        expect(httpInterceptSpy.delete).toHaveBeenCalledWith(`${environment.apiUrl}/notification-groups/123-456`);
        expect(toastServiceSpy.onSuccess).toHaveBeenCalled();
    });

    it('should add members to a notification group', () => {
        const mockResponse = new HttpResponse({ body: null });
        httpInterceptSpy.post.and.returnValue(of(mockResponse));

        service.addMembers('123-456', ['new1@example.com', 'new2@example.com']).subscribe();

        expect(httpInterceptSpy.post).toHaveBeenCalled();
        expect(toastServiceSpy.onSuccess).toHaveBeenCalled();
    });

    it('should remove a member from a notification group', () => {
        const mockResponse = new HttpResponse({ body: null });
        httpInterceptSpy.delete.and.returnValue(of(mockResponse));

        service.removeMember('123-456', 'test@example.com').subscribe();

        expect(httpInterceptSpy.delete).toHaveBeenCalledWith(
            `${environment.apiUrl}/notification-groups/123-456/members/test@example.com`
        );
        expect(toastServiceSpy.onSuccess).toHaveBeenCalled();
    });

    it('should handle create group error', () => {
        httpInterceptSpy.post.and.returnValue(of(new HttpResponse({ body: null, status: 400 })));

        service.createGroup('Test', 'Test', []).subscribe({
            error: () => {
                expect(toastServiceSpy.onError).toHaveBeenCalled();
            }
        });
    });

    it('should handle update group error', () => {
        httpInterceptSpy.put.and.returnValue(of(new HttpResponse({ body: null, status: 400 })));

        service.updateGroup('123', 'Test', 'Test').subscribe({
            error: () => {
                expect(toastServiceSpy.onError).toHaveBeenCalled();
            }
        });
    });
});


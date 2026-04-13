import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { NotificationGroupService } from './notification-group.service';
import { HttpInterceptorService } from './http-interceptor.service';
import { ToastService } from './toast.service';
import { environment } from '../../environment';
import { HttpResponse } from '@angular/common/http';
import { NotificationGroup } from '../models/notification-group.model';
import { of, throwError } from 'rxjs';

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

    it('should create a new notification group with success toast', () => {
        const mockResponse = new HttpResponse({ body: mockGroup });
        httpInterceptSpy.post.and.returnValue(of(mockResponse));

        service.createGroup('New Group', 'New Description', ['new@example.com']).subscribe(response => {
            expect(response.body).toEqual(mockGroup);
        });

        expect(toastServiceSpy.onSuccess).toHaveBeenCalledWith('Notification group created successfully');
    });

    it('should handle create group error', (done) => {
        const error = new Error('API Error');
        httpInterceptSpy.post.and.returnValue(throwError(() => error));

        service.createGroup('Test', 'Test', []).subscribe({
            error: (err) => {
                expect(toastServiceSpy.onError).toHaveBeenCalledWith('Failed to create notification group');
                done();
            }
        });
    });

    it('should update an existing notification group with success toast', () => {
        const mockResponse = new HttpResponse({ body: mockGroup });
        httpInterceptSpy.put.and.returnValue(of(mockResponse));

        service.updateGroup('123-456', 'Updated Group', 'Updated Description').subscribe(response => {
            expect(response.body).toEqual(mockGroup);
        });

        expect(toastServiceSpy.onSuccess).toHaveBeenCalledWith('Notification group updated successfully');
    });

    it('should handle update group error', (done) => {
        const error = new Error('API Error');
        httpInterceptSpy.put.and.returnValue(throwError(() => error));

        service.updateGroup('123', 'Test', 'Test').subscribe({
            error: (err) => {
                expect(toastServiceSpy.onError).toHaveBeenCalledWith('Failed to update notification group');
                done();
            }
        });
    });

    it('should delete a notification group with success toast', () => {
        const mockResponse = new HttpResponse({ body: null });
        httpInterceptSpy.delete.and.returnValue(of(mockResponse));

        service.deleteGroup('123-456').subscribe();

        expect(httpInterceptSpy.delete).toHaveBeenCalledWith(`${environment.apiUrl}/notification-groups/123-456`);
        expect(toastServiceSpy.onSuccess).toHaveBeenCalledWith('Notification group deleted successfully');
    });

    it('should handle delete group error', (done) => {
        const error = new Error('API Error');
        httpInterceptSpy.delete.and.returnValue(throwError(() => error));

        service.deleteGroup('123').subscribe({
            error: (err) => {
                expect(toastServiceSpy.onError).toHaveBeenCalledWith('Failed to delete notification group');
                done();
            }
        });
    });

    it('should add members to a notification group with success toast', () => {
        const mockResponse = new HttpResponse({ body: null });
        httpInterceptSpy.post.and.returnValue(of(mockResponse));

        service.addMembers('123-456', ['new1@example.com', 'new2@example.com']).subscribe();

        expect(toastServiceSpy.onSuccess).toHaveBeenCalledWith('Members added to group successfully');
    });

    it('should handle add members error', (done) => {
        const error = new Error('API Error');
        httpInterceptSpy.post.and.returnValue(throwError(() => error));

        service.addMembers('123', ['test@example.com']).subscribe({
            error: (err) => {
                expect(toastServiceSpy.onError).toHaveBeenCalledWith('Failed to add members to group');
                done();
            }
        });
    });

    it('should remove a member from a notification group with success toast', () => {
        const mockResponse = new HttpResponse({ body: null });
        httpInterceptSpy.delete.and.returnValue(of(mockResponse));

        service.removeMember('123-456', 'test@example.com').subscribe();

        expect(httpInterceptSpy.delete).toHaveBeenCalledWith(
            `${environment.apiUrl}/notification-groups/123-456/members/test@example.com`
        );
        expect(toastServiceSpy.onSuccess).toHaveBeenCalledWith('Member removed from group successfully');
    });

    it('should handle remove member error', (done) => {
        const error = new Error('API Error');
        httpInterceptSpy.delete.and.returnValue(throwError(() => error));

        service.removeMember('123', 'test@example.com').subscribe({
            error: (err) => {
                expect(toastServiceSpy.onError).toHaveBeenCalledWith('Failed to remove member from group');
                done();
            }
        });
    });

    it('should get group by id', () => {
        service.getGroupById('123-456').subscribe();
    });
});



import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, tap, catchError, throwError } from 'rxjs';
import { environment } from '../../environment';
import { HttpInterceptorService } from './http-interceptor.service';
import { ToastService } from './toast.service';
import { NotificationGroup } from '../models/notification-group.model';

@Injectable({ providedIn: 'root' })
export class NotificationGroupService {
    private readonly httpInterceptService = inject(HttpInterceptorService);
    private readonly toast = inject(ToastService);
    private readonly http = inject(HttpClient);

    // Get all notification groups
    getAllGroups(): Observable<HttpResponse<NotificationGroup[]>> {
        return this.httpInterceptService.get<NotificationGroup[]>(
            `${environment.apiUrl}/notification-groups`
        );
    }

    // Get a specific notification group by ID
    getGroupById(id: string): Observable<NotificationGroup> {
        return this.http.get<NotificationGroup>(
            `${environment.apiUrl}/notification-groups/${id}`
        );
    }

    // Create a new notification group
    createGroup(name: string, description: string, members: string[]): Observable<HttpResponse<NotificationGroup>> {
        const request = {
            name,
            description,
            members: members || []
        };
        return this.httpInterceptService.post<NotificationGroup>(
            `${environment.apiUrl}/notification-groups`,
            request
        ).pipe(
            tap(() => this.toast.onSuccess('Notification group created successfully')),
            catchError(err => {
                this.toast.onError('Failed to create notification group');
                return throwError(() => err);
            })
        );
    }

    // Update an existing notification group
    updateGroup(id: string, name: string, description: string): Observable<HttpResponse<NotificationGroup>> {
        const request = {
            name,
            description
        };
        return this.httpInterceptService.put<NotificationGroup>(
            `${environment.apiUrl}/notification-groups/${id}`,
            request
        ).pipe(
            tap(() => this.toast.onSuccess('Notification group updated successfully')),
            catchError(err => {
                this.toast.onError('Failed to update notification group');
                return throwError(() => err);
            })
        );
    }

    // Delete a notification group
    deleteGroup(id: string): Observable<HttpResponse<void>> {
        return this.httpInterceptService.delete<void>(
            `${environment.apiUrl}/notification-groups/${id}`
        ).pipe(
            tap(() => this.toast.onSuccess('Notification group deleted successfully')),
            catchError(err => {
                this.toast.onError('Failed to delete notification group');
                return throwError(() => err);
            })
        );
    }

    // Add members to a group
    addMembers(groupId: string, emails: string[]): Observable<HttpResponse<void>> {
        const request = {
            emails
        };
        return this.httpInterceptService.post<void>(
            `${environment.apiUrl}/notification-groups/${groupId}/members`,
            request
        ).pipe(
            tap(() => this.toast.onSuccess('Members added to group successfully')),
            catchError(err => {
                this.toast.onError('Failed to add members to group');
                return throwError(() => err);
            })
        );
    }

    // Remove a member from a group
    removeMember(groupId: string, email: string): Observable<HttpResponse<void>> {
        return this.httpInterceptService.delete<void>(
            `${environment.apiUrl}/notification-groups/${groupId}/members/${email}`
        ).pipe(
            tap(() => this.toast.onSuccess('Member removed from group successfully')),
            catchError(err => {
                this.toast.onError('Failed to remove member from group');
                return throwError(() => err);
            })
        );
    }
}

import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialogRef } from '@angular/material/dialog';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of, throwError } from 'rxjs';
import { NotificationGroupsDialogComponent } from './notification-groups-dialog.component';
import { NotificationGroupService } from '../../../services/notification-group.service';
import { ToastService } from '../../../services/toast.service';
import { NotificationGroup } from '../../../models/notification-group.model';
import { HttpResponse } from '@angular/common/http';

describe('NotificationGroupsDialogComponent', () => {
    let component: NotificationGroupsDialogComponent;
    let fixture: ComponentFixture<NotificationGroupsDialogComponent>;
    let mockDialogRef: jasmine.SpyObj<MatDialogRef<NotificationGroupsDialogComponent>>;
    let groupServiceSpy: jasmine.SpyObj<NotificationGroupService>;
    let toastServiceSpy: jasmine.SpyObj<ToastService>;

    const mockGroups: NotificationGroup[] = [
        {
            id: '1',
            name: 'Group 1',
            description: 'Description 1',
            members: ['user1@example.com']
        }
    ];

    beforeEach(async () => {
        mockDialogRef = jasmine.createSpyObj('MatDialogRef', ['close']);
        groupServiceSpy = jasmine.createSpyObj('NotificationGroupService', [
            'getAllGroups',
            'createGroup',
            'updateGroup',
            'deleteGroup',
            'addMembers',
            'removeMember'
        ]);
        toastServiceSpy = jasmine.createSpyObj('ToastService', ['onSuccess', 'onError']);

        await TestBed.configureTestingModule({
            imports: [NotificationGroupsDialogComponent, HttpClientTestingModule],
            providers: [
                { provide: MatDialogRef, useValue: mockDialogRef },
                { provide: NotificationGroupService, useValue: groupServiceSpy },
                { provide: ToastService, useValue: toastServiceSpy },
                provideNoopAnimations()
            ]
        }).compileComponents();

        groupServiceSpy.getAllGroups.and.returnValue(of(new HttpResponse({ body: mockGroups })));

        fixture = TestBed.createComponent(NotificationGroupsDialogComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should load groups on initialization', () => {
        expect(groupServiceSpy.getAllGroups).toHaveBeenCalled();
        expect(component.groups().length).toBe(1);
    });

    it('should initialize form with empty values', () => {
        expect(component.form.get('name')?.value).toBe('');
        expect(component.form.get('description')?.value).toBe('');
    });

    it('should add email when form has valid email', () => {
        const initialLength = component.selectedGroupMembers().length;
        component.emailInputControl.setValue('test@example.com');

        component.addEmail();

        expect(component.selectedGroupMembers().length).toBeGreaterThan(initialLength);
        expect(component.selectedGroupMembers()).toContain('test@example.com');
    });

    it('should not add duplicate emails', () => {
        component.emailInputControl.setValue('test@example.com');
        component.addEmail();
        const length = component.selectedGroupMembers().length;

        component.emailInputControl.setValue('test@example.com');
        component.addEmail();

        expect(component.selectedGroupMembers().length).toBe(length);
    });

    it('should remove email from selected members', () => {
        component.selectedGroupMembers.set(['test1@example.com', 'test2@example.com']);

        component.removeEmail('test1@example.com');

        expect(component.selectedGroupMembers()).toEqual(['test2@example.com']);
    });

    it('should start edit mode when editing a group', () => {
        component.startEditGroup(mockGroups[0]);

        expect(component.isEditing()).toBe(true);
        expect(component.editingGroupId()).toBe('1');
    });

    it('should cancel edit mode', () => {
        component.editingGroupId.set('1');
        expect(component.isEditing()).toBe(true);

        component.cancelEdit();

        expect(component.isEditing()).toBe(false);
        expect(component.editingGroupId()).toBeNull();
    });

    it('should close dialog with groups', () => {
        component.closeDialog();

        expect(mockDialogRef.close).toHaveBeenCalledWith(mockGroups);
    });

    it('should load groups from service', () => {
        groupServiceSpy.getAllGroups.and.returnValue(of(new HttpResponse({ body: mockGroups })));

        component.loadGroups();

        expect(groupServiceSpy.getAllGroups).toHaveBeenCalled();
        expect(component.groups().length).toBe(1);
    });

    it('should create a group', () => {
        const mockResponse = new HttpResponse({ body: mockGroups[0] });
        groupServiceSpy.createGroup.and.returnValue(of(mockResponse));
        groupServiceSpy.getAllGroups.and.returnValue(of(new HttpResponse({ body: mockGroups })));

        component.form.patchValue({
            name: 'New Group',
            description: 'New Description',
            members: []
        });

        component.createGroup();

        expect(groupServiceSpy.createGroup).toHaveBeenCalled();
    });

    it('should delete a group with confirmation', () => {
        spyOn(window, 'confirm').and.returnValue(true);
        groupServiceSpy.deleteGroup.and.returnValue(of(new HttpResponse({ body: null })));
        groupServiceSpy.getAllGroups.and.returnValue(of(new HttpResponse({ body: mockGroups })));

        component.deleteGroup('1');

        expect(groupServiceSpy.deleteGroup).toHaveBeenCalledWith('1');
    });

    it('should handle save error with form validation', () => {
        component.editingGroupId.set('1');
        component.form.patchValue({
            name: '',
            description: 'Description'
        });

        component.saveGroupChanges();

        expect(component.loading()).toBe(false);
    });

    it('should not save when form is invalid', () => {
        groupServiceSpy.updateGroup.and.returnValue(of({ body: mockGroups[0] }));

        component.editingGroupId.set('1');
        component.form.patchValue({
            name: '',
            description: 'Invalid'
        });

        component.saveGroupChanges();

        expect(groupServiceSpy.updateGroup).not.toHaveBeenCalled();
    });

    it('should handle save error for invalid group id', () => {
        component.editingGroupId.set(null);
        component.form.patchValue({
            name: 'Test',
            description: 'Test'
        });

        component.saveGroupChanges();

        expect(groupServiceSpy.updateGroup).not.toHaveBeenCalled();
    });

    it('should handle group loading error', () => {
        spyOn(console, 'error');
        groupServiceSpy.getAllGroups.and.returnValue(throwError(() => new Error('Load failed')));

        component.loadGroups();

        expect(console.error).toHaveBeenCalledWith('Error loading groups:', jasmine.any(Error));
        expect(component.loading()).toBe(false);
    });

    it('should handle group creation error', () => {
        spyOn(console, 'error');
        groupServiceSpy.createGroup.and.returnValue(throwError(() => new Error('Create failed')));

        component.form.patchValue({
            name: 'Test',
            description: 'Test'
        });

        component.createGroup();

        expect(console.error).toHaveBeenCalledWith('Error creating group:', jasmine.any(Error));
        expect(component.loading()).toBe(false);
    });

    it('should handle group update error', () => {
        spyOn(console, 'error');
        groupServiceSpy.updateGroup.and.returnValue(throwError(() => new Error('Update failed')));

        component.editingGroupId.set('1');
        component.form.patchValue({
            name: 'Updated',
            description: 'Updated'
        });

        component.saveGroupChanges();

        expect(console.error).toHaveBeenCalledWith('Error updating group:', jasmine.any(Error));
        expect(component.loading()).toBe(false);
    });

    it('should handle group deletion error without confirmation', () => {
        spyOn(window, 'confirm').and.returnValue(false);

        component.deleteGroup('1');

        expect(groupServiceSpy.deleteGroup).not.toHaveBeenCalled();
    });

    it('should set loading state during operations', () => {
        const mockResponse = new HttpResponse({ body: mockGroups[0] });
        groupServiceSpy.createGroup.and.returnValue(of(mockResponse));

        component.form.patchValue({
            name: 'New Group'
        });

        component.createGroup();

        expect(component.loading()).toBeDefined();
    });

    it('should reset form after successful creation', () => {
        groupServiceSpy.createGroup.and.returnValue(of(new HttpResponse({ body: mockGroups[0] })));
        groupServiceSpy.getAllGroups.and.returnValue(of(new HttpResponse({ body: mockGroups })));

        component.form.patchValue({
            name: 'New Group',
            description: 'Desc',
            members: []
        });

        component.createGroup();

        expect(component.form.get('name')?.value).toBe('');
        expect(component.form.get('description')?.value).toBe('');
    });
}

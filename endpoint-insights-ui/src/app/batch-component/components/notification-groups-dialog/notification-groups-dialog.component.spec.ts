import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialogRef } from '@angular/material/dialog';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of } from 'rxjs';
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

    it('should not delete a group if not confirmed', () => {
        spyOn(window, 'confirm').and.returnValue(false);

        component.deleteGroup('1');

        expect(groupServiceSpy.deleteGroup).not.toHaveBeenCalled();
    });
});

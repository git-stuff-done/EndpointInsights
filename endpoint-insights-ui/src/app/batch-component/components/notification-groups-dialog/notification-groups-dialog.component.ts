import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatDivider } from '@angular/material/list';
import { MatSelectModule } from '@angular/material/select';
import { NotificationGroupService } from '../../../services/notification-group.service';
import { NotificationGroup } from '../../../models/notification-group.model';

@Component({
    selector: 'app-notification-groups-dialog',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatDialogModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatIconModule,
        MatSelectModule,
        MatDivider
    ],
    templateUrl: './notification-groups-dialog.component.html',
    styleUrls: ['./notification-groups-dialog.component.scss']
})
export class NotificationGroupsDialogComponent implements OnInit {
    private readonly fb = inject(FormBuilder);
    private readonly notificationGroupService = inject(NotificationGroupService);
    private readonly dialogRef = inject(MatDialogRef<NotificationGroupsDialogComponent>);

    // State management
    groups = signal<NotificationGroup[]>([]);
    loading = signal(false);
    editingGroupId = signal<string | null>(null);

    // Form controls
    groupNameControl = new FormControl('', [Validators.required, Validators.maxLength(255)]);
    descriptionControl = new FormControl('', [Validators.maxLength(500)]);
    emailInputControl = new FormControl('', [Validators.email]);
    selectedGroupMembers = signal<string[]>([]);

    form = this.fb.group({
        name: ['', [Validators.required, Validators.maxLength(255)]],
        description: ['', [Validators.maxLength(500)]],
        members: [[] as string[]]
    });

    ngOnInit(): void {
        this.loadGroups();
    }

    // Load all notification groups from the server
    loadGroups(): void {
        this.loading.set(true);
        this.notificationGroupService.getAllGroups().subscribe({
            next: (response) => {
                this.groups.set(response.body ?? []);
                this.loading.set(false);
            },
            error: (error) => {
                console.error('Error loading groups:', error);
                this.loading.set(false);
            }
        });
    }

    // Create a new group
    createGroup(): void {
        if (this.form.invalid) {
            return;
        }

        const { name, description, members } = this.form.value;
        this.loading.set(true);

        this.notificationGroupService.createGroup(
            name || '',
            description || '',
            members || []
        ).subscribe({
            next: () => {
                this.form.reset();
                this.selectedGroupMembers.set([]);
                this.loadGroups();
            },
            error: (error) => {
                console.error('Error creating group:', error);
                this.loading.set(false);
            }
        });
    }

    // Edit an existing group
    startEditGroup(group: NotificationGroup): void {
        this.editingGroupId.set(group.id);
        this.form.patchValue({
            name: group.name,
            description: group.description,
            members: group.members
        });
        this.selectedGroupMembers.set([...group.members]);
    }

    // Save group changes (update)
    saveGroupChanges(): void {
        if (this.form.invalid) {
            return;
        }

        const groupId = this.editingGroupId();
        if (!groupId) {
            return;
        }

        const { name, description } = this.form.value;
        this.loading.set(true);

        this.notificationGroupService.updateGroup(groupId, name || '', description || '').subscribe({
            next: () => {
                this.cancelEdit();
                this.loadGroups();
            },
            error: (error) => {
                console.error('Error updating group:', error);
                this.loading.set(false);
            }
        });
    }

    // Cancel editing
    cancelEdit(): void {
        this.editingGroupId.set(null);
        this.form.reset();
        this.selectedGroupMembers.set([]);
    }

    // Delete a group
    deleteGroup(groupId: string): void {
        if (confirm('Are you sure you want to delete this group?')) {
            this.loading.set(true);
            this.notificationGroupService.deleteGroup(groupId).subscribe({
                next: () => {
                    this.loadGroups();
                },
                error: (error) => {
                    console.error('Error deleting group:', error);
                    this.loading.set(false);
                }
            });
        }
    }

    // Add an email to the current group being edited/created
    addEmail(): void {
        const email = this.emailInputControl.value?.trim() ?? '';
        if (!email) {
            return;
        }

        if (!this.selectedGroupMembers().includes(email)) {
            this.selectedGroupMembers.update(members => [...members, email]);
            this.form.patchValue({ members: this.selectedGroupMembers() });
        }

        this.emailInputControl.setValue('');
    }

    // Remove an email from the current group
    removeEmail(email: string): void {
        this.selectedGroupMembers.update(members => members.filter(m => m !== email));
        this.form.patchValue({ members: this.selectedGroupMembers() });
    }

    // Check if currently editing a group
    isEditing(): boolean {
        return this.editingGroupId() !== null;
    }

    // Close the dialog
    closeDialog(): void {
        this.dialogRef.close(this.groups());
    }
}

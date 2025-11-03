import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogContent, MatDialogTitle, MatDialogActions, MatDialogClose } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { ModalConfig } from './modal.models';

@Component({
    selector: 'app-modal',
    standalone: true,
    imports: [
        CommonModule,
        MatDialogTitle, MatDialogContent, MatDialogActions, MatDialogClose,
        MatButtonModule, MatIconModule, MatTabsModule
    ],
    templateUrl: './modal.component.html',
    styleUrls: ['./modal.component.scss']
})
export class ModalComponent {
    constructor(@Inject(MAT_DIALOG_DATA) public data: ModalConfig) {}
    get tabs() { return this.data?.tabs ?? []; }
    get showTabHeader() { return this.tabs.length > 1; }
}

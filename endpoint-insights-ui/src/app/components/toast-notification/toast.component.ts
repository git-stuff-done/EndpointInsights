import {Component, Inject} from '@angular/core';
import {MAT_SNACK_BAR_DATA, MatSnackBarRef} from '@angular/material/snack-bar';
import {MatIcon} from "@angular/material/icon";
import {MatIconButton} from "@angular/material/button";

@Component({
    selector: 'toast',
    template: `
        <div class="toast">
            <div class="toast-content">
                <h3 class="type">{{ data.type }}</h3>
                <span class="content">{{ data.message }}</span>
            </div>
            <button mat-icon-button (click)="close()">
                <mat-icon class="close">close</mat-icon>
            </button>
        </div>
    `,
    imports: [
        MatIcon,
        MatIconButton
    ],

})
export class ToastComponent {
    constructor(@Inject(MAT_SNACK_BAR_DATA) public data: {type: string, message: string}, private toast: MatSnackBarRef<ToastComponent>) {}

    close() {
        this.toast.dismiss();
    }
}
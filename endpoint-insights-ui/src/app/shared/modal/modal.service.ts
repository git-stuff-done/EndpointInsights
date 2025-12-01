import { Injectable, inject } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ModalComponent } from './modal.component';
import { ModalConfig } from './modal.models';

@Injectable({ providedIn: 'root' })
export class ModalService {
    private dialog = inject(MatDialog);

    open(config: ModalConfig) {
        return this.dialog.open(ModalComponent, {
            data: config,
            width: config.width ?? '600px',
            maxWidth: config.maxWidth ?? '95vw',
            autoFocus: 'first-tabbable',
            restoreFocus: true,
            enterAnimationDuration: '150ms',
            exitAnimationDuration: '100ms'
        });
    }
}

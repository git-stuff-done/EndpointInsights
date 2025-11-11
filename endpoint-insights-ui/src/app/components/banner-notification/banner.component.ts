import { Component, OnInit, OnDestroy } from '@angular/core';
import { NotificationService, NotificationType } from '../../services/notification.service';
import { Subscription, timer } from 'rxjs';
import {NgClass} from "@angular/common";


@Component({
    selector: 'app-banner-notification',
    standalone: true,
    template: `
        @if (message) {
            <div class="banner" [ngClass]="type">
                <span>{{ message }}</span>
                <button class="close-btn" (click)="dismiss()">×</button>
            </div>
        }
    `,
    imports: [
        NgClass,
    ],
    styleUrls: ['./banner.component.scss']

})
export class BannerNotificationComponent implements OnInit, OnDestroy {
    message: string | null = null;
    type: NotificationType = 'info';
    private sub!: Subscription;
    private timerSub?: Subscription;

    constructor(private notifications: NotificationService) {}

    ngOnInit() {
        this.sub = this.notifications.banner$.subscribe(notif => {
            if (notif) {
                this.message = notif.message;
                this.type = notif.type;

                // Auto-dismiss after 5 seconds
                this.timerSub?.unsubscribe(); // cancel previous timer if any
                this.timerSub = timer(5000).subscribe(() => this.dismiss());
            } else {
                this.message = null;
            }
        });
    }

    dismiss() {
        this.message = null;
        this.notifications.clearBanner();
        this.timerSub?.unsubscribe();    }

    ngOnDestroy() {
        this.sub.unsubscribe();
        this.timerSub?.unsubscribe();
    }
}
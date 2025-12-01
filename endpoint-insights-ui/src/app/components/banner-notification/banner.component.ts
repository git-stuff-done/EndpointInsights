import { Component, OnInit, OnDestroy } from '@angular/core';
import { NotificationService, NotificationType } from '../../services/notification.service';
import { Subscription, timer } from 'rxjs';
import { NgClass } from '@angular/common';

@Component({
    selector: 'app-banner-notification',
    standalone: true,
    imports: [NgClass],
    templateUrl: './banner.component.html',
    styleUrls: ['./banner.component.scss'],
})
export class BannerNotificationComponent implements OnInit, OnDestroy {

    messages: { text: string; type: NotificationType }[] = [];
    private sub!: Subscription;

    constructor(private notifications: NotificationService) {}

    ngOnInit() {
        this.sub = this.notifications.banner$.subscribe(notif => {
            if (!notif) {
                this.messages = [];
                return;
            }

            const msg = { text: notif.message, type: notif.type };
            this.messages.push(msg);

            // Auto-dismiss after 5 sec
            timer(5000).subscribe(() => this.dismiss(msg));
        });
    }

    dismiss(msg: { text: string; type: NotificationType }) {
        this.messages = this.messages.filter(m => m !== msg);
        this.notifications.clearBanner();
    }

    ngOnDestroy() {
        this.sub.unsubscribe();
    }
}
import { Component } from '@angular/core';
import { BannerNotificationComponent } from '../components/banner-notification/banner.component';
import { NotificationService } from '../services/notification.service';
import {Router, RouterOutlet} from '@angular/router';

@Component({
    selector: 'app-login',
    imports: [BannerNotificationComponent, RouterOutlet],
    templateUrl: './login-component.html',
    styleUrl: './login-component.scss'
})
export class LoginComponent {
    constructor(private notifications: NotificationService, private router: Router) {}

    login() {
        const success = true;
        if (success) {
            this.notifications.showBanner('You have successfully logged in', 'success');
            setTimeout(() => {
                this.router.navigate(['/']);
            }, 2000);
        } else {
            this.notifications.showBanner('Login failed. Try again.', 'error');
        }
    }

    showBanner() {
        this.notifications.showBanner('You have successfully logged in', 'success');
    }
}
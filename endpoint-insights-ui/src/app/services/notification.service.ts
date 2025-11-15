import {Injectable} from "@angular/core";
import { ToastService } from './toast.service';
import { Subject } from "rxjs";

export type NotificationType = 'success' | 'error' | 'info' | 'warning';

@Injectable({ providedIn: 'root' })

export class NotificationService {
    private bannerSubject = new Subject<{ type: NotificationType; message: string }>();
    banner$ = this.bannerSubject.asObservable();
    constructor(private toastService: ToastService) {}

    // ------ Toast Notifications ------
    showToast(message: string, type: NotificationType = 'info', duration?: number) {
        switch (type) {
            case 'success':
                this.toastService.onSuccess(message, duration);
                break;
            case 'error':
                this.toastService.onError(message, duration);
                break;
            default:
                // fallback toast for info/warning
                this.toastService.onSuccess(`[${type.toUpperCase()}] ${message}`, duration);
                break;
        }
    }
    // ------ Banner Notifications ------
    showBanner(message: string, type: NotificationType = 'info') {
        this.bannerSubject.next({type, message});
    }

    clearBanner(){
        this.bannerSubject.next(null as any);
    }
}

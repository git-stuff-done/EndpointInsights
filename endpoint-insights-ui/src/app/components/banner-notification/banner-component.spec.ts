import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { BannerNotificationComponent } from './banner.component';
import { NotificationService, NotificationType } from '../../services/notification.service';
import { Subject } from 'rxjs';

describe('BannerNotificationComponent', () => {
    let component: BannerNotificationComponent;
    let fixture: ComponentFixture<BannerNotificationComponent>;
    let notificationService: jasmine.SpyObj<NotificationService>;
    let bannerSubject: Subject<{ message: string; type: NotificationType } | null>;

    beforeEach(async () => {
        bannerSubject = new Subject();
        notificationService = jasmine.createSpyObj('NotificationService', ['clearBanner'], {
            banner$: bannerSubject.asObservable()
        });

        await TestBed.configureTestingModule({
            imports: [BannerNotificationComponent],
            providers: [
                { provide: NotificationService, useValue: notificationService }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(BannerNotificationComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    afterEach(() => {
        fixture.destroy();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize with no message', () => {
        expect(component.message).toBeNull();
        expect(component.type).toBe('info');
    });

    it('should display message when notification is received', () => {
        bannerSubject.next({ message: 'Test message', type: 'success' });
        fixture.detectChanges();

        expect(component.message).toBe('Test message');
        expect(component.type).toBe('success');

        const bannerElement = fixture.nativeElement.querySelector('.banner');
        expect(bannerElement).toBeTruthy();
        expect(bannerElement.textContent).toContain('Test message');
    });

    it('should apply correct CSS class based on notification type', () => {
        bannerSubject.next({ message: 'Error message', type: 'error' });
        fixture.detectChanges();

        const bannerElement = fixture.nativeElement.querySelector('.banner');
        expect(bannerElement.classList.contains('error')).toBe(true);
    });

    it('should display different notification types', () => {
        const types: NotificationType[] = ['success', 'error', 'warning', 'info'];

        types.forEach(type => {
            bannerSubject.next({ message: `${type} message`, type });
            fixture.detectChanges();

            expect(component.type).toBe(type);
            const bannerElement = fixture.nativeElement.querySelector('.banner');
            expect(bannerElement.classList.contains(type)).toBe(true);
        });
    });

    it('should clear message when null notification is received', () => {
        // First show a message
        bannerSubject.next({ message: 'Test message', type: 'info' });
        fixture.detectChanges();
        expect(component.message).toBe('Test message');

        // Then clear it
        bannerSubject.next(null);
        fixture.detectChanges();
        expect(component.message).toBeNull();
    });

    it('should auto-dismiss after 5 seconds', fakeAsync(() => {
        bannerSubject.next({ message: 'Test message', type: 'info' });
        fixture.detectChanges();

        expect(component.message).toBe('Test message');

        tick(5000);

        expect(component.message).toBeNull();
        expect(notificationService.clearBanner).toHaveBeenCalled();
    }));

    it('should dismiss when close button is clicked', () => {
        bannerSubject.next({ message: 'Test message', type: 'info' });
        fixture.detectChanges();

        const closeButton = fixture.nativeElement.querySelector('.close-btn');
        expect(closeButton).toBeTruthy();

        closeButton.click();
        fixture.detectChanges();

        expect(component.message).toBeNull();
        expect(notificationService.clearBanner).toHaveBeenCalled();
    });

    it('should cancel previous timer when new notification arrives', fakeAsync(() => {
        // Show first notification
        bannerSubject.next({ message: 'First message', type: 'info' });
        fixture.detectChanges();
        tick(3000); // Wait 3 seconds

        // Show second notification (should cancel first timer)
        bannerSubject.next({ message: 'Second message', type: 'success' });
        fixture.detectChanges();
        tick(3000); // Wait 3 more seconds (total 6, but timer restarted)

        // Message should still be visible (only 3 seconds since second notification)
        expect(component.message).toBe('Second message');

        tick(2000); // Complete the 5 seconds for second notification

        // Now it should be dismissed
        expect(component.message).toBeNull();
    }));

    it('should unsubscribe timer when dismissed manually', fakeAsync(() => {
        bannerSubject.next({ message: 'Test message', type: 'info' });
        fixture.detectChanges();
        tick(2000);

        component.dismiss();
        fixture.detectChanges();

        expect(component.message).toBeNull();
        expect(notificationService.clearBanner).toHaveBeenCalled();

        // Timer should be cancelled, so waiting longer shouldn't cause issues
        tick(10000);
        expect(notificationService.clearBanner).toHaveBeenCalledTimes(1);
    }));

    it('should not display banner when message is null', () => {
        component.message = null;
        fixture.detectChanges();

        const bannerElement = fixture.nativeElement.querySelector('.banner');
        expect(bannerElement).toBeNull();
    });

    it('should unsubscribe on destroy', () => {
        spyOn(component['sub'], 'unsubscribe');

        component.ngOnDestroy();

        expect(component['sub'].unsubscribe).toHaveBeenCalled();
    });

    it('should unsubscribe timer on destroy if it exists', fakeAsync(() => {
        bannerSubject.next({ message: 'Test message', type: 'info' });
        fixture.detectChanges();
        tick(1000);

        const timerSpy = spyOn(component['timerSub']!, 'unsubscribe');

        component.ngOnDestroy();

        expect(timerSpy).toHaveBeenCalled();
    }));

    it('should handle multiple rapid notifications', fakeAsync(() => {
        bannerSubject.next({ message: 'Message 1', type: 'info' });
        fixture.detectChanges();
        tick(1000);

        bannerSubject.next({ message: 'Message 2', type: 'warning' });
        fixture.detectChanges();
        tick(1000);

        bannerSubject.next({ message: 'Message 3', type: 'error' });
        fixture.detectChanges();

        expect(component.message).toBe('Message 3');
        expect(component.type).toBe('error');

        tick(5000);
        expect(component.message).toBeNull();
    }));
});
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

    it('should initialize with no messages', () => {
        expect(component.messages.length).toBe(0);
    });

    it('should display message when notification is received', () => {
        bannerSubject.next({ message: 'Test message', type: 'success' });
        fixture.detectChanges();

        const lastMsg = component.messages[component.messages.length - 1];
        expect(lastMsg.text).toBe('Test message');
        expect(lastMsg.type).toBe('success');

        const bannerElement = fixture.nativeElement.querySelector('.banner');
        expect(bannerElement).toBeTruthy();
        expect(bannerElement.textContent).toContain('Test message');
    });

    it('should apply correct CSS class based on notification type', () => {
        bannerSubject.next({ message: 'Error message', type: 'error' });
        fixture.detectChanges();

        const lastMsg = component.messages[component.messages.length - 1];
        const bannerElement = fixture.nativeElement.querySelector('.banner');
        expect(bannerElement.classList.contains(lastMsg.type)).toBe(true);
    });

    it('should display different notification types', () => {
        const types: NotificationType[] = ['success', 'error', 'warning', 'info'];

        types.forEach(type => {
            bannerSubject.next({ message: `${type} message`, type });
            fixture.detectChanges();

            const lastMsg = component.messages[component.messages.length - 1];
            expect(lastMsg.type).toBe(type);

            const banners = fixture.nativeElement.querySelectorAll('.banner');
            const lastBanner = banners[banners.length - 1];
            expect(lastBanner.classList.contains(type)).toBe(true);
        });
    });

    it('should clear messages when null notification is received', () => {
        bannerSubject.next({ message: 'Test message', type: 'info' });
        fixture.detectChanges();
        expect(component.messages.length).toBe(1);

        bannerSubject.next(null);
        fixture.detectChanges();
        expect(component.messages.length).toBe(0);
    });

    it('should auto-dismiss after 5 seconds', fakeAsync(() => {
        bannerSubject.next({ message: 'Test message', type: 'info' });
        fixture.detectChanges();

        expect(component.messages.length).toBe(1);

        tick(5000);

        expect(component.messages.length).toBe(0);
        expect(notificationService.clearBanner).toHaveBeenCalled();
    }));

    it('should dismiss when close button is clicked', () => {
        bannerSubject.next({ message: 'Test message', type: 'info' });
        fixture.detectChanges();

        const closeButton = fixture.nativeElement.querySelector('.close-btn');
        expect(closeButton).toBeTruthy();

        closeButton.click();
        fixture.detectChanges();

        expect(component.messages.length).toBe(0);
        expect(notificationService.clearBanner).toHaveBeenCalled();
    });

    it('should cancel previous timer when new notification arrives', fakeAsync(() => {
        bannerSubject.next({ message: 'First message', type: 'info' });
        fixture.detectChanges();
        tick(3000);

        bannerSubject.next({ message: 'Second message', type: 'success' });
        fixture.detectChanges();
        tick(3000);

        const lastMsg = component.messages[component.messages.length - 1];
        expect(lastMsg.text).toBe('Second message');

        tick(2000);

        expect(component.messages.length).toBe(0);
    }));

    it('should unsubscribe on destroy', () => {
        spyOn(component['sub'], 'unsubscribe');

        component.ngOnDestroy();

        expect(component['sub'].unsubscribe).toHaveBeenCalled();
    });
});

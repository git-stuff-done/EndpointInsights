import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { LoginComponent } from './login-component';
import { NotificationService } from '../services/notification.service';
import { Component } from "@angular/core";
import { BannerNotificationComponent } from '../components/banner-notification/banner.component';

@Component({
    selector: 'app-banner-notification',
    template: '',
    standalone: true
})
class MockBannerNotificationComponent {}

describe('LoginComponent', () => {
    let component: LoginComponent;
    let fixture: ComponentFixture<LoginComponent>;
    let notificationSpy: jasmine.SpyObj<NotificationService>;
    let routerSpy: jasmine.SpyObj<Router>;

    beforeEach(async () => {
        notificationSpy = jasmine.createSpyObj('NotificationService', ['showBanner']);
        routerSpy = jasmine.createSpyObj('Router', ['navigate']);

        await TestBed.configureTestingModule({
            imports: [LoginComponent],
            providers: [
                { provide: NotificationService, useValue: notificationSpy },
                { provide: Router, useValue: routerSpy },
            ]
        })
            .overrideComponent(LoginComponent, {
                remove: { imports: [BannerNotificationComponent] },
                add: { imports: [MockBannerNotificationComponent] }
            })
            .compileComponents();

        fixture = TestBed.createComponent(LoginComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should show success banner on login', () => {
        component.login();
        expect(notificationSpy.showBanner).toHaveBeenCalledWith(
            'You have successfully logged in',
            'success'
        );
    });

    it('should navigate after login', (done) => {
        component.login();
        setTimeout(() => {
            expect(routerSpy.navigate).toHaveBeenCalledWith(['/']);
            done();
        }, 2100);
    });

    it('should show banner when showBanner() is called', () => {
        component.showBanner();
        expect(notificationSpy.showBanner).toHaveBeenCalledWith(
            'You have successfully logged in',
            'success'
        );
    });
});
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router } from '@angular/router';
import { LoginComponent } from './login-component';
import { NotificationService } from '../services/notification.service';
import { Component } from "@angular/core";
import { BannerNotificationComponent } from '../components/banner-notification/banner.component';
import {provideHttpClient} from "@angular/common/http";


describe('LoginComponent', () => {
    let component: LoginComponent;
    let fixture: ComponentFixture<LoginComponent>;

    beforeEach(async () => {

        await TestBed.configureTestingModule({
            imports: [LoginComponent],
            providers: [provideHttpClient()]
        }).compileComponents();

        fixture = TestBed.createComponent(LoginComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

});
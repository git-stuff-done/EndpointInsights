import { TestBed } from '@angular/core/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { AppComponent } from './app';
import {provideHttpClient} from "@angular/common/http";

describe('App', () => {
    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [
                RouterTestingModule, // <— provides Router, ActivatedRoute, etc.
                AppComponent,        // standalone component
            ],
            providers: [provideHttpClient()]
        }).compileComponents();
    });

    it('should create the app', () => {
        const fixture = TestBed.createComponent(AppComponent);
        expect(fixture.componentInstance).toBeTruthy();
    });

});

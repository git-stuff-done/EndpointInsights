
import { Component, signal, computed } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatMenuModule } from '@angular/material/menu';
import { MatIconModule } from '@angular/material/icon';
import { MatDividerModule } from '@angular/material/divider';
import {AuthenticationService} from "./services/authentication.service";

@Component({
    selector: 'app-root',
    standalone: true,
    imports: [RouterLink, RouterLinkActive, MatButtonModule, MatMenuModule, MatIconModule, MatDividerModule, RouterOutlet],
    templateUrl: './app.html',
    styleUrls: ['./app.scss'],
})
export class AppComponent {
    readonly title = signal('endpoint-insights-ui'); // needed for the test

    constructor(public authService: AuthenticationService) {}

    get userInitials(): string {
        const info = this.authService.getUserInfo();
        if (!info?.username) return '?';
        return info.username.charAt(0).toUpperCase();
    }
}

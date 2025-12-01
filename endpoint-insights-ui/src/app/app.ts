
import { Component, signal } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { MatButtonModule, MatButton } from '@angular/material/button';
import {AuthenticationService} from "./services/authentication.service";

@Component({
    selector: 'app-root',
    standalone: true,
    imports: [RouterLink, RouterLinkActive, MatButtonModule, RouterOutlet],
    templateUrl: './app.html',
    styleUrls: ['./app.scss'],
})
export class AppComponent {
    readonly title = signal('endpoint-insights-ui'); // needed for the test
    constructor(public authService: AuthenticationService) {}

}

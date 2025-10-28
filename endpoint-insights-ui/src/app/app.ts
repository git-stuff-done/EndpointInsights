// app.ts
import { Component, signal } from '@angular/core';
import { RouterOutlet, RouterLink, RouterLinkActive } from '@angular/router';
import { MatButtonModule, MatButton } from '@angular/material/button';

@Component({
    selector: 'app-root',
    standalone: true,
    imports: [RouterLink, RouterLinkActive, MatButtonModule],
    templateUrl: './app.html',
    styleUrls: ['./app.scss'],
})
export class AppComponent {
    readonly title = signal('endpoint-insights-ui'); // needed for the test
}

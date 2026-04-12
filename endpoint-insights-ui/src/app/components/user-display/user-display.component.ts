import { Component, input } from '@angular/core';
import { MatTooltip } from '@angular/material/tooltip';

@Component({
  selector: 'app-user-display',
  template: `
    <span
      class="user-display"
      [matTooltip]="tooltipText()"
      matTooltipPosition="above"
      matTooltipClass="user-tooltip">
      {{ displayName() }}
    </span>
  `,
  styles: [`
    .user-display {
      text-decoration: underline;
      text-decoration-style: dotted;
      text-underline-offset: 3px;
      cursor: help;
    }
  `],
  imports: [MatTooltip]
})
export class UserDisplayComponent {
  name = input.required<string | null>();
  subject = input.required<string>();
  issuer = input.required<string>();

  displayName() {
    return this.name() || this.subject();
  }

  tooltipText() {
    return `${this.subject()}@${this.issuer()}`;
  }
}

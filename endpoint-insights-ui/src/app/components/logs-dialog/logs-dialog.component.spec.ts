import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LogsDialog } from './logs-dialog';

describe('LogsDialog', () => {
  let component: LogsDialog;
  let fixture: ComponentFixture<LogsDialog>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LogsDialog]
    })
    .compileComponents();

    fixture = TestBed.createComponent(LogsDialog);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LogsDialogComponent, LogsDialogData } from './logs-dialog.component';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { Clipboard } from '@angular/cdk/clipboard';
import { NoopAnimationsModule } from '@angular/platform-browser/animations';

describe('LogsDialogComponent', () => {
  let fixture: ComponentFixture<LogsDialogComponent>;
  let component: LogsDialogComponent;

  const dialogData: LogsDialogData = {
    title: 'Logs — Test A',
    logsText: '1  [INFO] Hello\n2  [ERROR] Boom',
    fileName: 'logs-test-a.txt',
  };

  const dialogRefMock = {
    close: jasmine.createSpy('close'),
  };

  const clipboardMock = {
    copy: jasmine.createSpy('copy').and.returnValue(true),
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LogsDialogComponent, NoopAnimationsModule], // standalone component goes in imports
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: dialogData },
        { provide: MatDialogRef, useValue: dialogRefMock },
        { provide: Clipboard, useValue: clipboardMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(LogsDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render the title', () => {
    const el: HTMLElement = fixture.nativeElement;
    expect(el.textContent).toContain('Logs — Test A');
  });

  it('copyAll should copy full logsText', () => {
    component.copyAll();
    expect(clipboardMock.copy).toHaveBeenCalledWith(dialogData.logsText);
  });

  it('close should close the dialog', () => {
    component.close();
    expect(dialogRefMock.close).toHaveBeenCalled();
  });

  it('download should create an anchor click (smoke test)', () => {
    // Spy on DOM APIs used by download()
    const clickSpy = jasmine.createSpy('click');
    spyOn(document, 'createElement').and.returnValue({ click: clickSpy } as any);

    const urlSpy = spyOn(URL, 'createObjectURL').and.returnValue('blob:mock');
    const revokeSpy = spyOn(URL, 'revokeObjectURL');

    component.download();

    expect(urlSpy).toHaveBeenCalled();
    expect(clickSpy).toHaveBeenCalled();
    expect(revokeSpy).toHaveBeenCalledWith('blob:mock');
  });
});

import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { of } from 'rxjs';
import { MatDialog } from '@angular/material/dialog';

import { TestOverview } from './test-overview';
import { CreateJobModal } from '../../components/create-job-modal/create-job-modal';
import { EditJobModal } from '../../components/edit-job-modal/edit-job-modal';
import { MOCK_TESTS, TestItem } from '../../models/test.model';

describe('TestOverview', () => {
  let component: TestOverview;
  let fixture: ComponentFixture<TestOverview>;
  let dialogSpy: jasmine.SpyObj<MatDialog>;

  beforeEach(waitForAsync(() => {
    dialogSpy = jasmine.createSpyObj('MatDialog', ['open']);

    TestBed.configureTestingModule({
      imports: [TestOverview], // standalone component
      providers: [{ provide: MatDialog, useValue: dialogSpy }],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestOverview);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize tests from MOCK_TESTS', () => {
    expect(component.tests).toEqual(MOCK_TESTS);
    expect(component.tests.length).toBe(MOCK_TESTS.length);
  });

  it('onFilter logs to console', () => {
    const logSpy = spyOn(console, 'log');
    component.onFilter();
    expect(logSpy).toHaveBeenCalledWith('Filter Button clicked');
  });

  it('onOpen logs to console', () => {
    const logSpy = spyOn(console, 'log');
    const t = component.tests[0] as TestItem;
    component.onOpen(t);
    expect(logSpy).toHaveBeenCalledWith('Open Clicked');
  });

  it('onRun logs to console', () => {
    const logSpy = spyOn(console, 'log');
    const t = component.tests[0] as TestItem;
    component.onRun(t);
    expect(logSpy).toHaveBeenCalledWith('Run Clicked');
  });

  it('onDelete logs to console', () => {
    const logSpy = spyOn(console, 'log');
    const t = component.tests[0] as TestItem;
    component.onDelete(t);
    expect(logSpy).toHaveBeenCalledWith('Delete Clicked');
  });

  it('onEdit calls openEditModal with the test', () => {
    const t = component.tests[0] as TestItem;
    const openEditSpy = spyOn(component, 'openEditModal');
    component.onEdit(t);
    expect(openEditSpy).toHaveBeenCalledWith(t);
  });

  it('openCreateJobModal opens CreateJobModal with correct config', () => {
    dialogSpy.open.and.returnValue({ afterClosed: () => of(null) } as any);

    component.openCreateJobModal();

    expect(dialogSpy.open).toHaveBeenCalledWith(CreateJobModal, jasmine.objectContaining({
      width: '600px',
      maxWidth: '95vw',
    }));
  });

  it('openCreateJobModal logs when dialog returns a truthy result', () => {
    const logSpy = spyOn(console, 'log');
    const result = { id: 'abc' };
    dialogSpy.open.and.returnValue({ afterClosed: () => of(result) } as any);

    component.openCreateJobModal();

    expect(logSpy).toHaveBeenCalledWith('New job created:', result);
  });

  it('openCreateJobModal does not log when dialog returns a falsy result', () => {
    const logSpy = spyOn(console, 'log');
    dialogSpy.open.and.returnValue({ afterClosed: () => of(undefined) } as any);

    component.openCreateJobModal();

    // It may have other logs from other tests; ensure it wasn't called with this message
    expect(logSpy).not.toHaveBeenCalledWith('New job created:', jasmine.anything());
  });

  it('openEditModal opens EditJobModal with correct config and data', () => {
    const t = component.tests[0] as TestItem;
    dialogSpy.open.and.returnValue({ afterClosed: () => of(null) } as any);

    component.openEditModal(t);

    expect(dialogSpy.open).toHaveBeenCalledWith(EditJobModal, jasmine.objectContaining({
      width: '600px',
      maxWidth: '95vw',
      data: t,
    }));
  });

  it('openEditModal logs when dialog returns a truthy result', () => {
    const logSpy = spyOn(console, 'log');
    const t = component.tests[0] as TestItem;
    const result = { updated: true };

    dialogSpy.open.and.returnValue({ afterClosed: () => of(result) } as any);

    component.openEditModal(t);

    expect(logSpy).toHaveBeenCalledWith('New job created:', result);
  });

  it('openEditModal does not log when dialog returns a falsy result', () => {
    const logSpy = spyOn(console, 'log');
    const t = component.tests[0] as TestItem;

    dialogSpy.open.and.returnValue({ afterClosed: () => of(null) } as any);

    component.openEditModal(t);

    expect(logSpy).not.toHaveBeenCalledWith('New job created:', jasmine.anything());
  });
});

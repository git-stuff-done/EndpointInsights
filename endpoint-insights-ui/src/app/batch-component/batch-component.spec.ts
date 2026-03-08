import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import { of, Subscription } from 'rxjs';
import { HttpResponse } from '@angular/common/http';
import { MatDialog } from '@angular/material/dialog';

import { BatchComponent } from './batch-component';
import { Batch } from '../models/batch.model';
import { BatchService } from '../services/batch.service';
import { BatchStore } from '../services/batch-store.service';
import { BatchConfigDialogComponent } from './components/batch-config-dialog/batch-config-dialog.component';

describe('BatchComponent', () => {
  let component: BatchComponent;
  let fixture: ComponentFixture<BatchComponent>;
  let mockBatchService: jasmine.SpyObj<BatchService>;
  let mockDialog: jasmine.SpyObj<MatDialog>;
  let mockStore: jasmine.SpyObj<BatchStore>;

  const sampleBatches: Batch[] = [
    { id: '1', batchName: 'Batch 1', startTime: '', active: true, lastRunTime: '', nextRunTime: '', nextRunDate: '', notificationList: [], jobs: [], isNew: false } as Batch,
    { id: '2', batchName: 'Batch 2', startTime: '', active: false, lastRunTime: '', nextRunTime: '', nextRunDate: '', notificationList: [], jobs: [], isNew: false } as Batch,
  ];

  beforeEach(waitForAsync(() => {
    mockBatchService = jasmine.createSpyObj('BatchService', ['getAllBatches']);
    mockDialog = jasmine.createSpyObj('MatDialog', ['open']);
    mockStore = jasmine.createSpyObj('BatchStore', ['/* methods if needed */']);

    // default getAllBatches to return sample batches wrapped in HttpResponse
    mockBatchService.getAllBatches.and.returnValue(of(new HttpResponse<Batch[]>({ body: sampleBatches })));

    TestBed.configureTestingModule({
      imports: [BatchComponent], // component is standalone
      providers: [
        { provide: BatchService, useValue: mockBatchService },
        { provide: MatDialog, useValue: mockDialog },
        { provide: BatchStore, useValue: mockStore },
      ],
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(BatchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('ngOnInit should call batchService.getAllBatches and set batch', () => {
    // ngOnInit already called by fixture.detectChanges() in this setup, but call explicitly to be safe
    component.ngOnInit();
    expect(mockBatchService.getAllBatches).toHaveBeenCalled();
    expect(component.batch).toEqual(sampleBatches);
  });

  it('loadBatches should set batch from service response', () => {
    // change the return value to a different set and call loadBatches
    const other: Batch[] = [
      { id: '3', batchName: 'Batch 3', startTime: '', active: false, lastRunTime: '', nextRunTime: '', nextRunDate: '', notificationList: [], jobs: [], isNew: false } as Batch,
    ];
    mockBatchService.getAllBatches.and.returnValue(of(new HttpResponse<Batch[]>({ body: other })));

    component.loadBatches();
    expect(mockBatchService.getAllBatches).toHaveBeenCalled();
    expect(component.batch).toEqual(other);
  });

  it('trackById returns batch id', () => {
    const result = (component as any).trackById(0, sampleBatches[0]);
    expect(result).toBe('1');
  });

  it('onConfigure opens dialog and calls loadBatches after dialog closes', () => {
    // Spy on loadBatches
    const loadSpy = spyOn(component, 'loadBatches');

    // mock dialog.open(...).afterClosed() returning observable
    const afterClosed = of(true);
    mockDialog.open.and.returnValue({ afterClosed: () => afterClosed } as any);

    component.onConfigure(sampleBatches[0]);

    expect(mockDialog.open).toHaveBeenCalledWith(BatchConfigDialogComponent, jasmine.objectContaining({
      width: '900px',
      height: 'auto',
      data: sampleBatches[0]
    }));
    // since afterClosed emits true, loadBatches should be called
    expect(loadSpy).toHaveBeenCalled();
  });

  it('openCreateBatchModal opens dialog with isNew true and reloads on truthy result', () => {
    const loadSpy = spyOn(component, 'loadBatches');

    const dialogRefMock = {
      afterClosed: () => of(true)
    };
    mockDialog.open.and.returnValue(dialogRefMock as any);

    component.openCreateBatchModal();

    expect(mockDialog.open).toHaveBeenCalledWith(BatchConfigDialogComponent, jasmine.objectContaining({
      width: '900px',
      height: 'auto',
      data: jasmine.objectContaining({ isNew: true })
    }));
    expect(loadSpy).toHaveBeenCalled();
  });

  it('openCreateBatchModal does not call loadBatches when dialog result is falsy', () => {
    const loadSpy = spyOn(component, 'loadBatches');

    const dialogRefMock = {
      afterClosed: () => of(null)
    };
    mockDialog.open.and.returnValue(dialogRefMock as any);

    component.openCreateBatchModal();

    expect(loadSpy).not.toHaveBeenCalled();
  });

  it('ngOnDestroy unsubscribes from sub when present', () => {
    // give component a subscription and spy on unsubscribe
    const sub = new Subscription();
    spyOn(sub, 'unsubscribe');
    // access private sub via any
    (component as any).sub = sub;

    component.ngOnDestroy();

    expect(sub.unsubscribe).toHaveBeenCalled();
  });

  it('onDelete and onFilter simply log to console (sanity)', () => {
    const consoleSpy = spyOn(console, 'log');
    component.onDelete(sampleBatches[0]);
    component.onFilter();
    expect(consoleSpy).toHaveBeenCalled(); // at least one call
  });
});
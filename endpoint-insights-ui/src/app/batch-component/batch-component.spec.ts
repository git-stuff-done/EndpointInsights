import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { of, Subscription } from 'rxjs';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { BatchComponent } from './batch-component';
import { BatchService } from '../services/batch.service';
import { Batch } from '../models/batch.model';
import { BatchStore } from '../services/batch-store.service';
import { BatchConfigDialogComponent } from './components/batch-config-dialog/batch-config-dialog.component';

describe('BatchComponent', () => {
  let component: BatchComponent;
  let fixture: ComponentFixture<BatchComponent>;
  let mockBatchService: jasmine.SpyObj<BatchService>;
  let mockDialog: jasmine.SpyObj<MatDialog>;
  let mockStore: jasmine.SpyObj<BatchStore>;

  const mockBatches: Batch[] = [
    { id: '1', batchName: 'Nightly Build', startTime: new Date().toISOString(), active: true, lastRunTime: '', notificationList: [], jobs: [], isNew: false },
    { id: '2', batchName: 'Weekly Report', startTime: new Date().toISOString(), active: false, lastRunTime: '', notificationList: [], jobs: [], isNew: false },
    { id: '3', batchName: 'Auth Tests', startTime: new Date().toISOString(), active: true, lastRunTime: '', notificationList: [], jobs: [], isNew: false },
  ];

  beforeEach(async () => {
    mockBatchService = jasmine.createSpyObj('BatchService', ['getAllBatches']);
    mockDialog = jasmine.createSpyObj('MatDialog', ['open']);
    mockStore = jasmine.createSpyObj('BatchStore', []);
    mockBatchService.getAllBatches.and.returnValue(of(new HttpResponse({ body: mockBatches })));

    await TestBed.configureTestingModule({
      imports: [BatchComponent, HttpClientTestingModule],
      providers: [
        provideNoopAnimations(),
        { provide: BatchService, useValue: mockBatchService },
        { provide: MatDialog, useValue: mockDialog },
        { provide: BatchStore, useValue: mockStore },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(BatchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load batches on init', () => {
    expect(mockBatchService.getAllBatches).toHaveBeenCalled();
    expect(component.batch.length).toBe(3);
  });

  describe('filteredBatches', () => {
    it('should return all batches when search is empty and filter is "all"', () => {
      expect(component.filteredBatches.length).toBe(3);
    });

    it('should filter by name (case-insensitive)', () => {
      component.searchControl.setValue('nightly');
      expect(component.filteredBatches.length).toBe(1);
      expect(component.filteredBatches[0].batchName).toBe('Nightly Build');
    });

    it('should return empty when search matches nothing', () => {
      component.searchControl.setValue('zzzzz');
      expect(component.filteredBatches.length).toBe(0);
    });

    it('should return only active batches when filter is "active"', () => {
      component.setStatusFilter('active');
      expect(component.filteredBatches.length).toBe(2);
      expect(component.filteredBatches.every(b => b.active)).toBeTrue();
    });

    it('should return only inactive batches when filter is "inactive"', () => {
      component.setStatusFilter('inactive');
      expect(component.filteredBatches.length).toBe(1);
      expect(component.filteredBatches[0].batchName).toBe('Weekly Report');
    });

    it('should apply both search and status filter together', () => {
      component.searchControl.setValue('weekly');
      component.setStatusFilter('active');
      expect(component.filteredBatches.length).toBe(0);
    });
  });

  describe('hasActiveFilter', () => {
    it('should return false when statusFilter is "all"', () => {
      expect(component.hasActiveFilter).toBeFalse();
    });

    it('should return true when statusFilter is "active" or "inactive"', () => {
      component.setStatusFilter('active');
      expect(component.hasActiveFilter).toBeTrue();

      component.setStatusFilter('inactive');
      expect(component.hasActiveFilter).toBeTrue();
    });
  });

  it('loadBatches should set batch from service response', () => {
    const other: Batch[] = [
      { id: '4', batchName: 'Batch 4', startTime: '', active: false, lastRunTime: '', nextRunTime: '', nextRunDate: '', notificationList: [], jobs: [], isNew: false } as Batch,
    ];
    mockBatchService.getAllBatches.and.returnValue(of(new HttpResponse<Batch[]>({ body: other })));

    component.loadBatches();
    expect(component.batch).toEqual(other);
  });

  it('trackById returns batch id', () => {
    expect(component.trackById(0, mockBatches[0])).toBe('1');
  });

  it('onConfigure opens dialog and calls loadBatches after dialog closes', () => {
    const loadSpy = spyOn(component, 'loadBatches');
    mockDialog.open.and.returnValue({ afterClosed: () => of(true) } as any);

    component.onConfigure(mockBatches[0]);

    expect(mockDialog.open).toHaveBeenCalledWith(BatchConfigDialogComponent, jasmine.objectContaining({
      width: '900px',
      height: 'auto',
      data: mockBatches[0],
    }));
    expect(loadSpy).toHaveBeenCalled();
  });

  it('openCreateBatchModal opens dialog with isNew true and reloads on truthy result', () => {
    const loadSpy = spyOn(component, 'loadBatches');
    mockDialog.open.and.returnValue({ afterClosed: () => of(true) } as any);

    component.openCreateBatchModal();

    expect(mockDialog.open).toHaveBeenCalledWith(BatchConfigDialogComponent, jasmine.objectContaining({
      width: '900px',
      height: 'auto',
      data: jasmine.objectContaining({ isNew: true }),
    }));
    expect(loadSpy).toHaveBeenCalled();
  });

  it('openCreateBatchModal does not call loadBatches when dialog result is falsy', () => {
    const loadSpy = spyOn(component, 'loadBatches');
    mockDialog.open.and.returnValue({ afterClosed: () => of(null) } as any);

    component.openCreateBatchModal();

    expect(loadSpy).not.toHaveBeenCalled();
  });

  it('ngOnDestroy unsubscribes from sub when present', () => {
    const sub = new Subscription();
    spyOn(sub, 'unsubscribe');
    (component as any).sub = sub;

    component.ngOnDestroy();

    expect(sub.unsubscribe).toHaveBeenCalled();
  });

  it('onDelete and onFilter simply log to console', () => {
    const consoleSpy = spyOn(console, 'log');
    component.onDelete(mockBatches[0]);
    component.onFilter();
    expect(consoleSpy).toHaveBeenCalled();
  });
});

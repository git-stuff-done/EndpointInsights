import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { of } from 'rxjs';
import { HttpResponse } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { BatchComponent } from './batch-component';
import { BatchService } from '../services/batch.service';
import { Batch } from '../models/batch.model';

describe('BatchComponent', () => {
  let component: BatchComponent;
  let fixture: ComponentFixture<BatchComponent>;
  let mockBatchService: jasmine.SpyObj<BatchService>;

  const mockBatches: Batch[] = [
    { id: '1', batchName: 'Nightly Build', startTime: new Date().toISOString(), active: true, jobs: [] },
    { id: '2', batchName: 'Weekly Report', startTime: new Date().toISOString(), active: false, jobs: [] },
    { id: '3', batchName: 'Auth Tests', startTime: new Date().toISOString(), active: true, jobs: [] },
  ];

  beforeEach(async () => {
    mockBatchService = jasmine.createSpyObj('BatchService', ['getAllBatches']);
    mockBatchService.getAllBatches.and.returnValue(of(new HttpResponse({ body: mockBatches })));

    await TestBed.configureTestingModule({
      imports: [BatchComponent, HttpClientTestingModule],
      providers: [
        provideNoopAnimations(),
        { provide: BatchService, useValue: mockBatchService },
        { provide: MatDialog, useValue: jasmine.createSpyObj('MatDialog', ['open']) },
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
});

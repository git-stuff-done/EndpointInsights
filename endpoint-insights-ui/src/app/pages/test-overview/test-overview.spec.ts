import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { provideNoopAnimations } from '@angular/platform-browser/animations';
import { TestOverview } from './test-overview';
import { TestItem } from '../../models/test.model';

describe('TestOverview', () => {
  let component: TestOverview;
  let fixture: ComponentFixture<TestOverview>;

  const mockTests: TestItem[] = [
    {
      id: '1', name: 'Auth Login', batch: 'Nightly-01', description: 'login test',
      gitUrl: 'git.com/test', runCommand: 'npm test', compileCommand: 'npm build',
      jobType: 'E2E', createdAt: new Date(), createdBy: 'Alex', status: 'RUNNING',
    },
    {
      id: '2', name: 'Billing Refund', batch: 'Nightly-01', description: 'billing desc',
      gitUrl: 'git.com/test', runCommand: 'npm test', compileCommand: 'npm build',
      jobType: 'nightwatch', createdAt: new Date(), createdBy: 'Sam', status: 'STOPPED',
    },
    {
      id: '3', name: 'Payment API', batch: 'Nightly-02', description: '',
      gitUrl: 'git.com/test', runCommand: 'npm test', compileCommand: 'npm build',
      jobType: 'jmeter', createdAt: new Date(), createdBy: 'Bob', status: 'FAILED',
    },
  ];

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestOverview],
      providers: [
        provideNoopAnimations(),
        { provide: MatDialog, useValue: jasmine.createSpyObj('MatDialog', ['open']) },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(TestOverview);
    component = fixture.componentInstance;
    component.tests = [...mockTests];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('filteredTests', () => {
    it('should return all tests when search is empty and no status selected', () => {
      expect(component.filteredTests.length).toBe(3);
    });

    it('should filter by name (case-insensitive)', () => {
      component.searchControl.setValue('auth');
      expect(component.filteredTests.length).toBe(1);
      expect(component.filteredTests[0].name).toBe('Auth Login');
    });

    it('should filter by description', () => {
      component.searchControl.setValue('billing desc');
      expect(component.filteredTests.length).toBe(1);
      expect(component.filteredTests[0].id).toBe('2');
    });

    it('should return empty when search matches nothing', () => {
      component.searchControl.setValue('zzzzz');
      expect(component.filteredTests.length).toBe(0);
    });

    it('should filter by selected status', () => {
      component.toggleStatus('RUNNING');
      expect(component.filteredTests.length).toBe(1);
      expect(component.filteredTests[0].status).toBe('RUNNING');
    });

    it('should support multiple selected statuses', () => {
      component.toggleStatus('RUNNING');
      component.toggleStatus('STOPPED');
      expect(component.filteredTests.length).toBe(2);
    });

    it('should apply both search and status filter together', () => {
      component.searchControl.setValue('auth');
      component.toggleStatus('STOPPED');
      expect(component.filteredTests.length).toBe(0);
    });
  });

  describe('toggleStatus', () => {
    it('should add a status', () => {
      component.toggleStatus('RUNNING');
      expect(component.selectedStatuses.has('RUNNING')).toBeTrue();
    });

    it('should remove a status when toggled again', () => {
      component.toggleStatus('RUNNING');
      component.toggleStatus('RUNNING');
      expect(component.selectedStatuses.has('RUNNING')).toBeFalse();
    });
  });

  describe('hasActiveFilters', () => {
    it('should return false when no statuses are selected', () => {
      expect(component.hasActiveFilters).toBeFalse();
    });

    it('should return true when a status is selected', () => {
      component.toggleStatus('RUNNING');
      expect(component.hasActiveFilters).toBeTrue();
    });
  });
});

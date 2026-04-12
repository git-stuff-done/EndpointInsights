import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PerformanceChart } from './performance-chart';

describe('PerformanceChart', () => {
  let component: PerformanceChart;
  let fixture: ComponentFixture<PerformanceChart>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PerformanceChart]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PerformanceChart);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

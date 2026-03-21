import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewResult } from './view-result';

describe('ViewResult', () => {
  let component: ViewResult;
  let fixture: ComponentFixture<ViewResult>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ViewResult]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ViewResult);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

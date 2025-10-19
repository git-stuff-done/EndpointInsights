import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TestCardComponent } from './test-card.component';

describe('TestCardComponent', () => {
  let component: TestCardComponent;
  let fixture: ComponentFixture<TestCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestCardComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TestCardComponent);
    component = fixture.componentInstance;

    // Set required input
    component.test = {
      id: 'test-1',
      name: 'Test Name',
      status: 'PASS'
    };

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

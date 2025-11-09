import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CreateJobModal } from './create-job-modal';

describe('CreateJobModal', () => {
  let component: CreateJobModal;
  let fixture: ComponentFixture<CreateJobModal>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CreateJobModal]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CreateJobModal);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

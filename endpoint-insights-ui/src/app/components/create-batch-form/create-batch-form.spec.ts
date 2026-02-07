import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {CreateBatchForm} from './create-batch-form';
import {provideNoopAnimations} from '@angular/platform-browser/animations';

describe('CreateBatchForm', () => {
    let component: CreateBatchForm;
    let fixture: ComponentFixture<CreateBatchForm>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [CreateBatchForm, ReactiveFormsModule],
            providers: [provideNoopAnimations()]
        })
            .compileComponents();

        fixture = TestBed.createComponent(CreateBatchForm);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    describe('Form Initialization', () => {
        it('should initialize form with empty values', () => {
            expect(component.createBatchForm.get('title')?.value).toBe('');
            expect(component.createBatchForm.get('description')?.value).toBe('');
            expect(component.createBatchForm.get('testIds')?.value).toEqual([]);

        });

        it('should have all required form controls', () => {
            expect(component.createBatchForm.get('title')).toBeTruthy();
            expect(component.createBatchForm.get('description')).toBeTruthy();
            expect(component.createBatchForm.get('testIds')).toBeTruthy();

        });

        it('should be invalid when empty', () => {
            expect(component.createBatchForm.valid).toBeFalsy();
        });
    });

    describe('Title Field Validation', () => {
        it('should be invalid when title is empty', () => {
            const titleControl = component.createBatchForm.get('title');
            expect(titleControl?.hasError('required')).toBeTruthy();
        });

        it('should be invalid when title is less than 3 characters', () => {
            const titleControl = component.createBatchForm.get('title');
            titleControl?.setValue('ab');
            expect(titleControl?.hasError('minlength')).toBeTruthy();
        });

        it('should be invalid when title is more than 50 characters', () => {
            const titleControl = component.createBatchForm.get('title');
            titleControl?.setValue('a'.repeat(51));
            expect(titleControl?.hasError('maxlength')).toBeTruthy();
        });

        it('should be invalid when title contains invalid characters', () => {
            const titleControl = component.createBatchForm.get('title');
            titleControl?.setValue('invalid title!@#');
            expect(titleControl?.hasError('pattern')).toBeTruthy();
        });

        it('should be valid with valid title', () => {
            const titleControl = component.createBatchForm.get('title');
            titleControl?.setValue('valid-batch_title123');
            expect(titleControl?.valid).toBeTruthy();
        });
    });

    describe('Description Field Validation', () => {
        it('should be valid when description is empty', () => {
            const descControl = component.createBatchForm.get('description');
            expect(descControl?.valid).toBeTruthy();
        });

        it('should be invalid when description exceeds 500 characters', () => {
            const descControl = component.createBatchForm.get('description');
            descControl?.setValue('a'.repeat(501));
            expect(descControl?.hasError('maxlength')).toBeTruthy();
        });

        it('should be valid with description up to 500 characters', () => {
            const descControl = component.createBatchForm.get('description');
            descControl?.setValue('a'.repeat(500));
            expect(descControl?.valid).toBeTruthy();
        });
    });

    describe('Tests Dropdown Field Validation', () => {
        it('should be invalid when no tests are selected', () => {
            const testIdsControl = component.createBatchForm.get('testIds');

            testIdsControl?.setValue([]);
            testIdsControl?.markAsTouched();

            expect(testIdsControl?.hasError('required')).toBeTruthy();
        });

        it('should be valid when at least one test is selected', () => {
            const testIdsControl = component.createBatchForm.get('testIds');
            testIdsControl?.setValue(['t1']);   // one selected test

            expect(testIdsControl?.valid).toBeTruthy();
        });

        it('should be valid with multiple tests selected', () => {
            const testIdsControl = component.createBatchForm.get('testIds');

            testIdsControl?.setValue(['t1', 't2']);

            expect(testIdsControl?.valid).toBeTruthy();
        });
    });

    describe('getErrorMessage', () => {
        it('should return empty string when control is pristine', () => {
            const titleControl = component.createBatchForm.get('title');
            expect(component.getErrorMessage('title')).toBe('');
        });

        it('should return empty string when control is not touched', () => {
            const titleControl = component.createBatchForm.get('title');
            titleControl?.markAsPristine();
            expect(component.getErrorMessage('title')).toBe('');
        });

        it('should return required error message', () => {
            const titleControl = component.createBatchForm.get('title');
            titleControl?.markAsTouched();
            expect(component.getErrorMessage('title')).toBe('Batch Title is required');
        });

        it('should return minlength error message', () => {
            const titleControl = component.createBatchForm.get('title');
            titleControl?.setValue('ab');
            titleControl?.markAsTouched();
            expect(component.getErrorMessage('title')).toBe('Minimum length is 3');
        });

        it('should return maxlength error message', () => {
            const titleControl = component.createBatchForm.get('title');
            titleControl?.setValue('a'.repeat(51));
            titleControl?.markAsTouched();
            expect(component.getErrorMessage('title')).toBe('Maximum length is 50 characters');
        });

        it('should return pattern error message', () => {
            const titleControl = component.createBatchForm.get('title');
            titleControl?.setValue('invalid title!');
            titleControl?.markAsTouched();
            expect(component.getErrorMessage('title')).toBe('Only letters, numbers, hyphens, and underscores are allowed');
        });

        it('should return "Invalid input" for unknown error', () => {
            const titleControl = component.createBatchForm.get('title');
            titleControl?.setErrors({unknownError: true});
            titleControl?.markAsTouched();
            expect(component.getErrorMessage('title')).toBe('Invalid input');
        });
    });

    describe('submitForm', () => {
        it('should emit batchSubmitted event when form is valid', () => {
            spyOn(component.batchSubmitted, 'emit');

            component.createBatchForm.patchValue({
                title: 'test-batch',
                description: 'Test description',
                testIds: ["t1"],
            });

            component.submitForm();

            expect(component.batchSubmitted.emit).toHaveBeenCalledWith(component.createBatchForm.value);
        });

        it('should not emit batchSubmitted event when form is invalid', () => {
            spyOn(component.batchSubmitted, 'emit');

            component.createBatchForm.patchValue({
                title: '',
                description: '',
                testIds: [],
            });

            component.submitForm();

            expect(component.batchSubmitted.emit).not.toHaveBeenCalled();
        });

        it('should mark all fields as touched when form is invalid', () => {
            component.submitForm();

            expect(component.createBatchForm.get('title')?.touched).toBeTruthy();
            expect(component.createBatchForm.get('description')?.touched).toBeTruthy();
            expect(component.createBatchForm.get('testIds')?.touched).toBeTruthy();
        });
    });

    describe('Form Integration', () => {
        it('should be valid when all fields are filled correctly', () => {
            component.createBatchForm.patchValue({
                title: 'test-batch',
                description: 'Test description',
                testIds: ["t1"],
            });

            expect(component.createBatchForm.valid).toBeTruthy();
        });

        it('should be invalid if any required field is missing', () => {
            component.createBatchForm.patchValue({
                title: 'test-batch',
                description: 'Test description',
                testIds: [],
            });

            expect(component.createBatchForm.valid).toBeFalsy();
        });
    });
});
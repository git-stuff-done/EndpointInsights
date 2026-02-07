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
            expect(component.createBatchForm.get('name')?.value).toBe('');
            expect(component.createBatchForm.get('description')?.value).toBe('');
            expect(component.createBatchForm.get('gitUrl')?.value).toBe('');
            expect(component.createBatchForm.get('batchType')?.value).toBe('');
            expect(component.createBatchForm.get('runCommand')?.value).toBe('');
            expect(component.createBatchForm.get('compileCommand')?.value).toBe('');
        });

        it('should have all required form controls', () => {
            expect(component.createBatchForm.get('name')).toBeTruthy();
            expect(component.createBatchForm.get('description')).toBeTruthy();
            expect(component.createBatchForm.get('gitUrl')).toBeTruthy();
            expect(component.createBatchForm.get('batchType')).toBeTruthy();
            expect(component.createBatchForm.get('runCommand')).toBeTruthy();
            expect(component.createBatchForm.get('compileCommand')).toBeTruthy();
        });

        it('should be invalid when empty', () => {
            expect(component.createBatchForm.valid).toBeFalsy();
        });
    });

    describe('Name Field Validation', () => {
        it('should be invalid when name is empty', () => {
            const nameControl = component.createBatchForm.get('name');
            expect(nameControl?.hasError('required')).toBeTruthy();
        });

        it('should be invalid when name is less than 3 characters', () => {
            const nameControl = component.createBatchForm.get('name');
            nameControl?.setValue('ab');
            expect(nameControl?.hasError('minlength')).toBeTruthy();
        });

        it('should be invalid when name is more than 50 characters', () => {
            const nameControl = component.createBatchForm.get('name');
            nameControl?.setValue('a'.repeat(51));
            expect(nameControl?.hasError('maxlength')).toBeTruthy();
        });

        it('should be invalid when name contains invalid characters', () => {
            const nameControl = component.createBatchForm.get('name');
            nameControl?.setValue('invalid name!@#');
            expect(nameControl?.hasError('pattern')).toBeTruthy();
        });

        it('should be valid with valid name', () => {
            const nameControl = component.createBatchForm.get('name');
            nameControl?.setValue('valid-batch_name123');
            expect(nameControl?.valid).toBeTruthy();
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

    describe('Git URL Field Validation', () => {
        it('should be invalid when gitUrl is empty', () => {
            const gitUrlControl = component.createBatchForm.get('gitUrl');
            expect(gitUrlControl?.hasError('required')).toBeTruthy();
        });

        it('should be valid with http URL', () => {
            const gitUrlControl = component.createBatchForm.get('gitUrl');
            gitUrlControl?.setValue('http://github.com/user/repo.git');
            expect(gitUrlControl?.valid).toBeTruthy();
        });

        it('should be valid with https URL', () => {
            const gitUrlControl = component.createBatchForm.get('gitUrl');
            gitUrlControl?.setValue('https://github.com/user/repo.git');
            expect(gitUrlControl?.valid).toBeTruthy();
        });

        it('should be valid with ssh protocol URL', () => {
            const gitUrlControl = component.createBatchForm.get('gitUrl');
            gitUrlControl?.setValue('ssh://git@github.com/user/repo.git');
            expect(gitUrlControl?.valid).toBeTruthy();
        });

        it('should be valid with git@host:path format', () => {
            const gitUrlControl = component.createBatchForm.get('gitUrl');
            gitUrlControl?.setValue('git@github.com:user/repo.git');
            expect(gitUrlControl?.valid).toBeTruthy();
        });

        it('should be invalid with invalid git URL format', () => {
            const gitUrlControl = component.createBatchForm.get('gitUrl');
            gitUrlControl?.setValue('invalid-url');
            expect(gitUrlControl?.hasError('invalidGitUrl')).toBeTruthy();
        });
    });

    describe('Batch Type Field Validation', () => {
        it('should be invalid when batchType is empty', () => {
            const batchTypeControl = component.createBatchForm.get('batchType');
            expect(batchTypeControl?.hasError('required')).toBeTruthy();
        });

        it('should be valid with valid batchType', () => {
            const batchTypeControl = component.createBatchForm.get('batchType');
            batchTypeControl?.setValue('jmeter');
            expect(batchTypeControl?.valid).toBeTruthy();
        });
    });

    describe('Run Command Field Validation', () => {
        it('should be invalid when runCommand is empty', () => {
            const runCommandControl = component.createBatchForm.get('runCommand');
            expect(runCommandControl?.hasError('required')).toBeTruthy();
        });

        it('should be invalid when runCommand is less than 3 characters', () => {
            const runCommandControl = component.createBatchForm.get('runCommand');
            runCommandControl?.setValue('ab');
            expect(runCommandControl?.hasError('minlength')).toBeTruthy();
        });

        it('should be invalid when runCommand exceeds 500 characters', () => {
            const runCommandControl = component.createBatchForm.get('runCommand');
            runCommandControl?.setValue('a'.repeat(501));
            expect(runCommandControl?.hasError('maxlength')).toBeTruthy();
        });

        it('should be invalid when runCommand is only whitespace', () => {
            const runCommandControl = component.createBatchForm.get('runCommand');
            runCommandControl?.setValue('   ');
            expect(runCommandControl?.hasError('whitespace')).toBeTruthy();
        });

        it('should be valid with valid runCommand', () => {
            const runCommandControl = component.createBatchForm.get('runCommand');
            runCommandControl?.setValue('npm run test');
            expect(runCommandControl?.valid).toBeTruthy();
        });
    });

    describe('Compile Command Field Validation', () => {
        it('should be invalid when compileCommand is empty', () => {
            const compileCommandControl = component.createBatchForm.get('compileCommand');
            expect(compileCommandControl?.hasError('required')).toBeTruthy();
        });

        it('should be invalid when compileCommand is less than 3 characters', () => {
            const compileCommandControl = component.createBatchForm.get('compileCommand');
            compileCommandControl?.setValue('ab');
            expect(compileCommandControl?.hasError('minlength')).toBeTruthy();
        });

        it('should be invalid when compileCommand exceeds 500 characters', () => {
            const compileCommandControl = component.createBatchForm.get('compileCommand');
            compileCommandControl?.setValue('a'.repeat(501));
            expect(compileCommandControl?.hasError('maxlength')).toBeTruthy();
        });

        it('should be invalid when compileCommand is only whitespace', () => {
            const compileCommandControl = component.createBatchForm.get('compileCommand');
            compileCommandControl?.setValue('   ');
            expect(compileCommandControl?.hasError('whitespace')).toBeTruthy();
        });

        it('should be valid with valid compileCommand', () => {
            const compileCommandControl = component.createBatchForm.get('compileCommand');
            compileCommandControl?.setValue('npm run build');
            expect(compileCommandControl?.valid).toBeTruthy();
        });
    });

    describe('getErrorMessage', () => {
        it('should return empty string when control is pristine', () => {
            const nameControl = component.createBatchForm.get('name');
            expect(component.getErrorMessage('name')).toBe('');
        });

        it('should return empty string when control is not touched', () => {
            const nameControl = component.createBatchForm.get('name');
            nameControl?.markAsPristine();
            expect(component.getErrorMessage('name')).toBe('');
        });

        it('should return required error message', () => {
            const nameControl = component.createBatchForm.get('name');
            nameControl?.markAsTouched();
            expect(component.getErrorMessage('name')).toBe('Batch Name is required');
        });

        it('should return minlength error message', () => {
            const nameControl = component.createBatchForm.get('name');
            nameControl?.setValue('ab');
            nameControl?.markAsTouched();
            expect(component.getErrorMessage('name')).toBe('Minimum length is 3');
        });

        it('should return maxlength error message', () => {
            const nameControl = component.createBatchForm.get('name');
            nameControl?.setValue('a'.repeat(51));
            nameControl?.markAsTouched();
            expect(component.getErrorMessage('name')).toBe('Maximum length is 50 characters');
        });

        it('should return pattern error message', () => {
            const nameControl = component.createBatchForm.get('name');
            nameControl?.setValue('invalid name!');
            nameControl?.markAsTouched();
            expect(component.getErrorMessage('name')).toBe('Only letters, numbers, hyphens, and underscores are allowed');
        });

        it('should return whitespace error message', () => {
            const runCommandControl = component.createBatchForm.get('runCommand');
            runCommandControl?.setValue('   ');
            runCommandControl?.markAsTouched();
            expect(component.getErrorMessage('runCommand')).toBe('Run command cannot be empty or only whitespace');
        });

        it('should return invalid git URL error message', () => {
            const gitUrlControl = component.createBatchForm.get('gitUrl');
            gitUrlControl?.setValue('invalid-url');
            gitUrlControl?.markAsTouched();
            expect(component.getErrorMessage('gitUrl')).toBe('Git URL must start with http://, https://, ssh://, or use git@host:path format');
        });

        it('should return "Invalid input" for unknown error', () => {
            const nameControl = component.createBatchForm.get('name');
            nameControl?.setErrors({unknownError: true});
            nameControl?.markAsTouched();
            expect(component.getErrorMessage('name')).toBe('Invalid input');
        });
    });

    describe('submitForm', () => {
        it('should emit batchSubmitted event when form is valid', () => {
            spyOn(component.batchSubmitted, 'emit');

            component.createBatchForm.patchValue({
                name: 'test-batch',
                description: 'Test description',
                gitUrl: 'https://github.com/user/repo.git',
                batchType: 'jmeter',
                runCommand: 'npm run test',
                compileCommand: 'npm run build'
            });

            component.submitForm();

            expect(component.batchSubmitted.emit).toHaveBeenCalledWith(component.createBatchForm.value);
        });

        it('should not emit batchSubmitted event when form is invalid', () => {
            spyOn(component.batchSubmitted, 'emit');

            component.createBatchForm.patchValue({
                name: '',
                description: '',
                gitUrl: '',
                batchType: '',
                runCommand: '',
                compileCommand: ''
            });

            component.submitForm();

            expect(component.batchSubmitted.emit).not.toHaveBeenCalled();
        });

        it('should mark all fields as touched when form is invalid', () => {
            component.submitForm();

            expect(component.createBatchForm.get('name')?.touched).toBeTruthy();
            expect(component.createBatchForm.get('description')?.touched).toBeTruthy();
            expect(component.createBatchForm.get('gitUrl')?.touched).toBeTruthy();
            expect(component.createBatchForm.get('batchType')?.touched).toBeTruthy();
            expect(component.createBatchForm.get('runCommand')?.touched).toBeTruthy();
            expect(component.createBatchForm.get('compileCommand')?.touched).toBeTruthy();
        });
    });

    describe('Form Integration', () => {
        it('should be valid when all fields are filled correctly', () => {
            component.createBatchForm.patchValue({
                name: 'test-batch',
                description: 'Test description',
                gitUrl: 'https://github.com/user/repo.git',
                batchType: 'jmeter',
                runCommand: 'npm run test',
                compileCommand: 'npm run build'
            });

            expect(component.createBatchForm.valid).toBeTruthy();
        });

        it('should be invalid if any required field is missing', () => {
            component.createBatchForm.patchValue({
                name: 'test-batch',
                description: 'Test description',
                gitUrl: 'https://github.com/user/repo.git',
                batchType: 'jmeter',
                runCommand: 'npm run test',
                compileCommand: ''
            });

            expect(component.createBatchForm.valid).toBeFalsy();
        });
    });
});
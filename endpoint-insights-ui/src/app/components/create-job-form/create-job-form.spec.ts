import {ComponentFixture, TestBed} from '@angular/core/testing';
import {SimpleChange} from '@angular/core';
import {ReactiveFormsModule} from '@angular/forms';
import {CreateJobForm} from './create-job-form';
import {provideNoopAnimations} from '@angular/platform-browser/animations';
import {TestItem} from '../../models/test.model';

describe('CreateJobForm', () => {
    let component: CreateJobForm;
    let fixture: ComponentFixture<CreateJobForm>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            imports: [CreateJobForm, ReactiveFormsModule],
            providers: [provideNoopAnimations()]
        })
            .compileComponents();

        fixture = TestBed.createComponent(CreateJobForm);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    describe('Form Initialization', () => {
        it('should initialize form with empty values', () => {
            expect(component.createJobForm.get('name')?.value).toBe('');
            expect(component.createJobForm.get('description')?.value).toBe('');
            expect(component.createJobForm.get('gitUrl')?.value).toBe('');
            expect(component.createJobForm.get('gitAuthType')?.value).toBe('NONE');
            expect(component.createJobForm.get('gitUsername')?.value).toBe('');
            expect(component.createJobForm.get('gitPassword')?.value).toBe('');
            expect(component.createJobForm.get('gitSshPrivateKey')?.value).toBe('');
            expect(component.createJobForm.get('gitSshPassphrase')?.value).toBe('');
            expect(component.createJobForm.get('jobType')?.value).toBe('');
            expect(component.createJobForm.get('runCommand')?.value).toBe('');
            expect(component.createJobForm.get('compileCommand')?.value).toBe('');
        });

        it('should have all required form controls', () => {
            expect(component.createJobForm.get('name')).toBeTruthy();
            expect(component.createJobForm.get('description')).toBeTruthy();
            expect(component.createJobForm.get('gitUrl')).toBeTruthy();
            expect(component.createJobForm.get('gitAuthType')).toBeTruthy();
            expect(component.createJobForm.get('gitUsername')).toBeTruthy();
            expect(component.createJobForm.get('gitPassword')).toBeTruthy();
            expect(component.createJobForm.get('gitSshPrivateKey')).toBeTruthy();
            expect(component.createJobForm.get('gitSshPassphrase')).toBeTruthy();
            expect(component.createJobForm.get('jobType')).toBeTruthy();
            expect(component.createJobForm.get('runCommand')).toBeTruthy();
            expect(component.createJobForm.get('compileCommand')).toBeTruthy();
        });

        it('should be invalid when empty', () => {
            expect(component.createJobForm.valid).toBeFalsy();
        });
    });

    describe('ngOnChanges', () => {
        it('should patch form values when job input changes', () => {
            const job: TestItem = {
                id: '1',
                name: 'Updated Job',
                batch: 'Batch A',
                description: 'Updated description',
                gitUrl: 'https://github.com/user/repo.git',
                gitAuthType: 'NONE',
                runCommand: 'npm run test',
                compileCommand: 'npm run build',
                jobType: 'jmeter',
                createdAt: new Date(),
                createdBy: 'user',
                status: 'RUNNING'
            };

            component.job = job;
            component.ngOnChanges({ job: new SimpleChange(null, job, true) });

            expect(component.createJobForm.get('name')?.value).toBe(job.name);
            expect(component.createJobForm.get('description')?.value).toBe(job.description);
            expect(component.createJobForm.get('gitUrl')?.value).toBe(job.gitUrl);
            expect(component.createJobForm.get('jobType')?.value).toBe(job.jobType);
            expect(component.createJobForm.get('runCommand')?.value).toBe(job.runCommand);
            expect(component.createJobForm.get('compileCommand')?.value).toBe(job.compileCommand);
        });
    });

    describe('Name Field Validation', () => {
        it('should be invalid when name is empty', () => {
            const nameControl = component.createJobForm.get('name');
            expect(nameControl?.hasError('required')).toBeTruthy();
        });

        it('should be invalid when name is less than 3 characters', () => {
            const nameControl = component.createJobForm.get('name');
            nameControl?.setValue('ab');
            expect(nameControl?.hasError('minlength')).toBeTruthy();
        });

        it('should be invalid when name is more than 50 characters', () => {
            const nameControl = component.createJobForm.get('name');
            nameControl?.setValue('a'.repeat(51));
            expect(nameControl?.hasError('maxlength')).toBeTruthy();
        });

        it('should be invalid when name contains invalid characters', () => {
            const nameControl = component.createJobForm.get('name');
            nameControl?.setValue('invalid name!@#');
            expect(nameControl?.hasError('pattern')).toBeTruthy();
        });

        it('should be valid with valid name', () => {
            const nameControl = component.createJobForm.get('name');
            nameControl?.setValue('valid-job_name123');
            expect(nameControl?.valid).toBeTruthy();
        });
    });

    describe('Description Field Validation', () => {
        it('should be valid when description is empty', () => {
            const descControl = component.createJobForm.get('description');
            expect(descControl?.valid).toBeTruthy();
        });

        it('should be invalid when description exceeds 500 characters', () => {
            const descControl = component.createJobForm.get('description');
            descControl?.setValue('a'.repeat(501));
            expect(descControl?.hasError('maxlength')).toBeTruthy();
        });

        it('should be valid with description up to 500 characters', () => {
            const descControl = component.createJobForm.get('description');
            descControl?.setValue('a'.repeat(500));
            expect(descControl?.valid).toBeTruthy();
        });
    });

    describe('Git URL Field Validation', () => {
        it('should be invalid when gitUrl is empty', () => {
            const gitUrlControl = component.createJobForm.get('gitUrl');
            expect(gitUrlControl?.hasError('required')).toBeTruthy();
        });

        it('should be valid with http URL', () => {
            const gitUrlControl = component.createJobForm.get('gitUrl');
            gitUrlControl?.setValue('http://github.com/user/repo.git');
            expect(gitUrlControl?.valid).toBeTruthy();
        });

        it('should be valid with https URL', () => {
            const gitUrlControl = component.createJobForm.get('gitUrl');
            gitUrlControl?.setValue('https://github.com/user/repo.git');
            expect(gitUrlControl?.valid).toBeTruthy();
        });

        it('should be valid with ssh protocol URL', () => {
            const gitUrlControl = component.createJobForm.get('gitUrl');
            gitUrlControl?.setValue('ssh://git@github.com/user/repo.git');
            expect(gitUrlControl?.valid).toBeTruthy();
        });

        it('should be valid with git@host:path format', () => {
            const gitUrlControl = component.createJobForm.get('gitUrl');
            gitUrlControl?.setValue('git@github.com:user/repo.git');
            expect(gitUrlControl?.valid).toBeTruthy();
        });

        it('should be invalid with invalid git URL format', () => {
            const gitUrlControl = component.createJobForm.get('gitUrl');
            gitUrlControl?.setValue('invalid-url');
            expect(gitUrlControl?.hasError('invalidGitUrl')).toBeTruthy();
        });
    });

    describe('Job Type Field Validation', () => {
        it('should be invalid when jobType is empty', () => {
            const jobTypeControl = component.createJobForm.get('jobType');
            expect(jobTypeControl?.hasError('required')).toBeTruthy();
        });

        it('should be valid with valid jobType', () => {
            const jobTypeControl = component.createJobForm.get('jobType');
            jobTypeControl?.setValue('jmeter');
            expect(jobTypeControl?.valid).toBeTruthy();
        });
    });

    describe('Git Auth Field Validation', () => {
        it('should require username and password for BASIC auth', () => {
            component.createJobForm.patchValue({
                gitAuthType: 'BASIC'
            });

            const usernameControl = component.createJobForm.get('gitUsername');
            const passwordControl = component.createJobForm.get('gitPassword');
            usernameControl?.setValue('');
            passwordControl?.setValue('');

            expect(usernameControl?.hasError('required')).toBeTruthy();
            expect(passwordControl?.hasError('required')).toBeTruthy();
        });

        it('should require private key for SSH_KEY auth', () => {
            component.createJobForm.patchValue({
                gitAuthType: 'SSH_KEY'
            });

            const keyControl = component.createJobForm.get('gitSshPrivateKey');
            keyControl?.setValue('');

            expect(keyControl?.hasError('required')).toBeTruthy();
        });
    });

    describe('Run Command Field Validation', () => {
        it('should be invalid when runCommand is empty', () => {
            const runCommandControl = component.createJobForm.get('runCommand');
            expect(runCommandControl?.hasError('required')).toBeTruthy();
        });

        it('should be invalid when runCommand is less than 3 characters', () => {
            const runCommandControl = component.createJobForm.get('runCommand');
            runCommandControl?.setValue('ab');
            expect(runCommandControl?.hasError('minlength')).toBeTruthy();
        });

        it('should be invalid when runCommand exceeds 500 characters', () => {
            const runCommandControl = component.createJobForm.get('runCommand');
            runCommandControl?.setValue('a'.repeat(501));
            expect(runCommandControl?.hasError('maxlength')).toBeTruthy();
        });

        it('should be invalid when runCommand is only whitespace', () => {
            const runCommandControl = component.createJobForm.get('runCommand');
            runCommandControl?.setValue('   ');
            expect(runCommandControl?.hasError('whitespace')).toBeTruthy();
        });

        it('should be valid with valid runCommand', () => {
            const runCommandControl = component.createJobForm.get('runCommand');
            runCommandControl?.setValue('npm run test');
            expect(runCommandControl?.valid).toBeTruthy();
        });
    });

    describe('Compile Command Field Validation', () => {
        it('should be invalid when compileCommand is empty', () => {
            const compileCommandControl = component.createJobForm.get('compileCommand');
            expect(compileCommandControl?.hasError('required')).toBeTruthy();
        });

        it('should be invalid when compileCommand is less than 3 characters', () => {
            const compileCommandControl = component.createJobForm.get('compileCommand');
            compileCommandControl?.setValue('ab');
            expect(compileCommandControl?.hasError('minlength')).toBeTruthy();
        });

        it('should be invalid when compileCommand exceeds 500 characters', () => {
            const compileCommandControl = component.createJobForm.get('compileCommand');
            compileCommandControl?.setValue('a'.repeat(501));
            expect(compileCommandControl?.hasError('maxlength')).toBeTruthy();
        });

        it('should be invalid when compileCommand is only whitespace', () => {
            const compileCommandControl = component.createJobForm.get('compileCommand');
            compileCommandControl?.setValue('   ');
            expect(compileCommandControl?.hasError('whitespace')).toBeTruthy();
        });

        it('should be valid with valid compileCommand', () => {
            const compileCommandControl = component.createJobForm.get('compileCommand');
            compileCommandControl?.setValue('npm run build');
            expect(compileCommandControl?.valid).toBeTruthy();
        });
    });

    describe('getErrorMessage', () => {
        it('should return empty string when control is pristine', () => {
            const nameControl = component.createJobForm.get('name');
            expect(component.getErrorMessage('name')).toBe('');
        });

        it('should return empty string when control is not touched', () => {
            const nameControl = component.createJobForm.get('name');
            nameControl?.markAsPristine();
            expect(component.getErrorMessage('name')).toBe('');
        });

        it('should return required error message', () => {
            const nameControl = component.createJobForm.get('name');
            nameControl?.markAsTouched();
            expect(component.getErrorMessage('name')).toBe('Job Name is required');
        });

        it('should return minlength error message', () => {
            const nameControl = component.createJobForm.get('name');
            nameControl?.setValue('ab');
            nameControl?.markAsTouched();
            expect(component.getErrorMessage('name')).toBe('Minimum length is 3');
        });

        it('should return maxlength error message', () => {
            const nameControl = component.createJobForm.get('name');
            nameControl?.setValue('a'.repeat(51));
            nameControl?.markAsTouched();
            expect(component.getErrorMessage('name')).toBe('Maximum length is 50 characters');
        });

        it('should return pattern error message', () => {
            const nameControl = component.createJobForm.get('name');
            nameControl?.setValue('invalid name!');
            nameControl?.markAsTouched();
            expect(component.getErrorMessage('name')).toBe('Only letters, numbers, hyphens, and underscores are allowed');
        });

        it('should return whitespace error message', () => {
            const runCommandControl = component.createJobForm.get('runCommand');
            runCommandControl?.setValue('   ');
            runCommandControl?.markAsTouched();
            expect(component.getErrorMessage('runCommand')).toBe('Run command cannot be empty or only whitespace');
        });

        it('should return invalid git URL error message', () => {
            const gitUrlControl = component.createJobForm.get('gitUrl');
            gitUrlControl?.setValue('invalid-url');
            gitUrlControl?.markAsTouched();
            expect(component.getErrorMessage('gitUrl')).toBe('Git URL must start with http://, https://, ssh://, or use git@host:path format');
        });

        it('should return "Invalid input" for unknown error', () => {
            const nameControl = component.createJobForm.get('name');
            nameControl?.setErrors({unknownError: true});
            nameControl?.markAsTouched();
            expect(component.getErrorMessage('name')).toBe('Invalid input');
        });
    });

    describe('submitForm', () => {
        it('should emit jobSubmitted event when form is valid', () => {
            spyOn(component.jobSubmitted, 'emit');

            component.createJobForm.patchValue({
                name: 'test-job',
                description: 'Test description',
                gitUrl: 'https://github.com/user/repo.git',
                gitAuthType: 'NONE',
                jobType: 'jmeter',
                runCommand: 'npm run test',
                compileCommand: 'npm run build'
            });

            component.submitForm();

            expect(component.jobSubmitted.emit).toHaveBeenCalledWith(component.createJobForm.value);
        });

        it('should not emit jobSubmitted event when form is invalid', () => {
            spyOn(component.jobSubmitted, 'emit');

            component.createJobForm.patchValue({
                name: '',
                description: '',
                gitUrl: '',
                gitAuthType: 'NONE',
                jobType: '',
                runCommand: '',
                compileCommand: ''
            });

            component.submitForm();

            expect(component.jobSubmitted.emit).not.toHaveBeenCalled();
        });

        it('should mark all fields as touched when form is invalid', () => {
            component.submitForm();

            expect(component.createJobForm.get('name')?.touched).toBeTruthy();
            expect(component.createJobForm.get('description')?.touched).toBeTruthy();
            expect(component.createJobForm.get('gitUrl')?.touched).toBeTruthy();
            expect(component.createJobForm.get('gitAuthType')?.touched).toBeTruthy();
            expect(component.createJobForm.get('jobType')?.touched).toBeTruthy();
            expect(component.createJobForm.get('runCommand')?.touched).toBeTruthy();
            expect(component.createJobForm.get('compileCommand')?.touched).toBeTruthy();
        });
    });

    describe('Form Integration', () => {
        it('should be valid when all fields are filled correctly', () => {
            component.createJobForm.patchValue({
                name: 'test-job',
                description: 'Test description',
                gitUrl: 'https://github.com/user/repo.git',
                gitAuthType: 'NONE',
                jobType: 'jmeter',
                runCommand: 'npm run test',
                compileCommand: 'npm run build'
            });

            expect(component.createJobForm.valid).toBeTruthy();
        });

        it('should be invalid if any required field is missing', () => {
            component.createJobForm.patchValue({
                name: 'test-job',
                description: 'Test description',
                gitUrl: 'https://github.com/user/repo.git',
                gitAuthType: 'NONE',
                jobType: 'jmeter',
                runCommand: 'npm run test',
                compileCommand: ''
            });

            expect(component.createJobForm.valid).toBeFalsy();
        });
    });
});
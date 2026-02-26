import {CommonModule} from '@angular/common';
import {Component, EventEmitter, Input, Output, SimpleChanges} from '@angular/core';
import {
    AbstractControl,
    FormBuilder,
    FormGroup,
    ReactiveFormsModule,
    ValidationErrors,
    Validators
} from "@angular/forms";
import {MatFormField} from "@angular/material/form-field";
import {MatInputModule} from "@angular/material/input";
import {MatOption, MatSelect} from "@angular/material/select";
import {TestItem} from "../../models/test.model";

@Component({
    selector: 'app-job-form',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MatFormField,
        MatInputModule,
        MatSelect,
        MatOption,
    ],
    templateUrl: './create-job-form.html',
    styleUrl: './create-job-form.scss',
})
export class CreateJobForm {
    createJobForm: FormGroup;
    @Input() job!: TestItem;
    constructor(private formBuilder: FormBuilder) {
        this.createJobForm = this.formBuilder.group({
            name: ["", [
                Validators.required,
                Validators.minLength(3),
                Validators.maxLength(50),
                Validators.pattern(/^[a-zA-Z0-9_-]+$/),
            ]],
            description: ["", [Validators.maxLength(500)]],
            gitUrl: ["", [Validators.required, this.gitUrlValidator]],
            gitAuthType: ["NONE"],
            gitUsername: [""],
            gitPassword: [""],
            gitSshPrivateKey: [""],
            gitSshPassphrase: [""],
            jobType: ["", [Validators.required]],
            runCommand: ["", [
                Validators.required,
                Validators.minLength(3),
                Validators.maxLength(500),
                this.noWhitespaceValidator
            ]],
            compileCommand: ["", [
                Validators.required,
                Validators.minLength(3),
                Validators.maxLength(500),
                this.noWhitespaceValidator
            ]],
        });

        this.applyAuthValidators(this.createJobForm.get('gitAuthType')?.value);
        this.createJobForm.get('gitAuthType')?.valueChanges.subscribe((value) => {
            this.applyAuthValidators(value);
        });
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes['job'] && this.job) {
            this.createJobForm.patchValue({
                name: this.job.name,
                description: this.job.description,
                gitUrl: this.job.gitUrl,
                gitAuthType: this.job.gitAuthType ?? 'NONE',
                gitUsername: this.job.gitUsername,
                gitPassword: this.job.gitPassword,
                gitSshPrivateKey: this.job.gitSshPrivateKey,
                gitSshPassphrase: this.job.gitSshPassphrase,
                jobType: this.job.jobType,
                runCommand: this.job.runCommand,
                compileCommand: this.job.compileCommand,
            });
        }
    }



    @Output() jobSubmitted = new EventEmitter<any>();

    getErrorMessage(fieldName: string): string {
        const control = this.createJobForm.get(fieldName);

        if (!control?.errors || !control.touched) {
            return '';
        }

        if (control.errors['required']) {
            return `${this.getFieldLabel(fieldName)} is required`;
        }

        if (control.errors['minlength']) {
            return `Minimum length is ${control.errors['minlength'].requiredLength}`;
        }

        if (control.errors['maxlength']) {
            return `Maximum length is ${control.errors['maxlength'].requiredLength} characters`;
        }

        if (control.errors['pattern']) {
            return 'Only letters, numbers, hyphens, and underscores are allowed';
        }

        if (control.errors['whitespace']) {
            return `${this.getFieldLabel(fieldName)} cannot be empty or only whitespace`;
        }

        if (control.errors['invalidGitUrl']) {
            return 'Git URL must start with http://, https://, ssh://, or use git@host:path format';
        }

        return 'Invalid input';
    }

    private getFieldLabel(fieldName: string): string {
        const labels: { [key: string]: string } = {
            'name': 'Job Name',
            'description': 'Description',
            'gitUrl': 'Git URL',
            'gitAuthType': 'Git auth type',
            'gitUsername': 'Git username',
            'gitPassword': 'Git password',
            'gitSshPrivateKey': 'SSH private key',
            'gitSshPassphrase': 'SSH passphrase',
            'jobType': 'Job type',
            'runCommand': 'Run command',
            'compileCommand': 'Compile command',
        };
        return labels[fieldName] || fieldName;
    }

    private noWhitespaceValidator(control: AbstractControl): ValidationErrors | null {
        if (!control.value) {
            return null;
        }
        const isWhitespace = control.value.trim().length === 0;
        return isWhitespace ? {whitespace: true} : null;
    }


    private gitUrlValidator(control: AbstractControl): ValidationErrors | null {
        if (!control.value) {
            return null;
        }

        const value = control.value.trim();

        if (value.startsWith('http://') || value.startsWith('https://')) {
            return null;
        }

        const sshProtocolPattern = /^ssh:\/\/[^@]+@[^\/]+\/.+$/;
        if (sshProtocolPattern.test(value)) {
            return null;
        }

        const gitSshPattern = /^[^@]+@[^:]+:.+$/;
        if (gitSshPattern.test(value)) {
            return null;
        }

        return {invalidGitUrl: true};
    }

    private applyAuthValidators(authType: string | null) {
        const usernameControl = this.createJobForm.get('gitUsername');
        const passwordControl = this.createJobForm.get('gitPassword');
        const keyControl = this.createJobForm.get('gitSshPrivateKey');

        if (authType === 'BASIC') {
            usernameControl?.setValidators([Validators.required, this.noWhitespaceValidator]);
            passwordControl?.setValidators([Validators.required, this.noWhitespaceValidator]);
            keyControl?.clearValidators();
        } else if (authType === 'SSH_KEY') {
            keyControl?.setValidators([Validators.required, this.noWhitespaceValidator]);
            usernameControl?.clearValidators();
            passwordControl?.clearValidators();
        } else {
            usernameControl?.clearValidators();
            passwordControl?.clearValidators();
            keyControl?.clearValidators();
        }

        usernameControl?.updateValueAndValidity({emitEvent: false});
        passwordControl?.updateValueAndValidity({emitEvent: false});
        keyControl?.updateValueAndValidity({emitEvent: false});
    }

    submitForm() {
        if (this.createJobForm.valid) {
            this.jobSubmitted.emit(this.createJobForm.value);
            //TODO: Trigger success notification and call backend.

        } else {
            this.createJobForm.markAllAsTouched();
            //TODO: Trigger error notification.
        }
    }
}

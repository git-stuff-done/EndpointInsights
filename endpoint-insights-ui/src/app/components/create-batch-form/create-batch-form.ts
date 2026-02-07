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
import {Batch} from "../../models/batch.model";
import { MOCK_TESTS, type TestItem } from '../../models/test.model';
import { CommonModule } from '@angular/common';


@Component({
    selector: 'app-batch-form',
    standalone: true,
    imports: [
        ReactiveFormsModule,
        MatFormField,
        MatInputModule,
        MatSelect,
        MatOption,
        CommonModule,
    ],
    templateUrl: './create-batch-form.html',
    styleUrl: './create-batch-form.scss',
})
export class CreateBatchForm {
    tests: TestItem[] = MOCK_TESTS;

    createBatchForm: FormGroup;
    @Input() batch!: Batch;
    constructor(private formBuilder: FormBuilder) {
        this.createBatchForm = this.formBuilder.group({
            title: ["", [
                Validators.required,
                Validators.minLength(3),
                Validators.maxLength(50),
                Validators.pattern(/^[a-zA-Z0-9_-]+$/),
            ]],
            description: ["", [Validators.maxLength(500)]],
            testIds: this.formBuilder.control<string[]>([], {
                nonNullable: true,
                validators: [Validators.required],
                }),
        })
    }

    ngOnChanges(changes: SimpleChanges) {
        if (changes['batch'] && this.batch) {
            this.createBatchForm.patchValue({
                title: this.batch.title,
                description: this.batch.description,
            });
        }
    }



    @Output() batchSubmitted = new EventEmitter<any>();

    getErrorMessage(fieldName: string): string {
        const control = this.createBatchForm.get(fieldName);

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

        return 'Invalid input';
    }

    private getFieldLabel(fieldName: string): string {
        const labels: { [key: string]: string } = {
            'title': 'Batch Name',
            'description': 'Description',
        };
        return labels[fieldName] || fieldName;
    }

    submitForm() {
        if (this.createBatchForm.valid) {
            this.batchSubmitted.emit(this.createBatchForm.value);
            //TODO: Trigger success notification and call backend.

        } else {
            this.createBatchForm.markAllAsTouched();
            //TODO: Trigger error notification.
        }
    }
}

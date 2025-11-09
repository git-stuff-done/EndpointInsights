import {Component, EventEmitter, Output} from '@angular/core';
import {FormBuilder, FormGroup, ReactiveFormsModule, Validators} from "@angular/forms";
import {MatFormField} from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import {MatOption, MatSelect} from "@angular/material/select";

@Component({
  selector: 'app-job-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    MatFormField,
    MatInputModule,
    MatSelect,
    MatOption,
  ],
  templateUrl: './job-form.html',
  styleUrl: './job-form.scss',
})
export class JobForm {
  jobForm: FormGroup;

  constructor(private formBuilder: FormBuilder) {
    this.jobForm = this.formBuilder.group({
      name: ["", [Validators.required]],
      description: [""],
      gitUrl: ["", [Validators.required]],
      jobType: ["", [Validators.required]],
      runCommand: ["", [Validators.required]],
      compileCommand: ["", [Validators.required]],
    })
  }

  @Output() jobSubmitted = new EventEmitter<any>();

  submitForm() {
    if (this.jobForm.valid) {
      console.log('Form submitted:', this.jobForm.value);
      this.jobSubmitted.emit(this.jobForm.value);
    } else {
      this.jobForm.markAllAsTouched();
      console.log('Form is invalid');
      // do the OTHER thing
    }
  }
}

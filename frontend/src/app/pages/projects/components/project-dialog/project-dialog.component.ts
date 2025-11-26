import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, FormGroup } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogTitle, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatOptionModule } from '@angular/material/core';
import { ProjectService } from '../../../../core/services/project.service';
import { Project } from '../../models/project.model';
import { ProjectCreateRequest, ProjectUpdateRequest } from '../../models/project-request.model';
import { ProjectStatus } from '../../models/project-status.enum';

export interface ProjectDialogData {
  mode: 'create' | 'edit';
  project?: Project;
}

@Component({
  selector: 'app-project-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogTitle, MatDialogContent, MatDialogActions,
    MatFormFieldModule, MatInputModule, MatSelectModule, MatOptionModule, MatButtonModule,
  ],
  templateUrl: './project-dialog.component.html',
  styleUrls: ['./project-dialog.component.scss'],
})
export class ProjectDialogComponent {
  readonly statuses: ProjectStatus[] = [ProjectStatus.ACTIVE, ProjectStatus.ARCHIVED];
  isEdit = false;

  // ВАЖНО: инициализируем форму в конструкторе, а не в поле класса
  form!: FormGroup;

  constructor(
    private readonly fb: FormBuilder,
    private readonly ref: MatDialogRef<ProjectDialogComponent>,
    private readonly service: ProjectService,
    @Inject(MAT_DIALOG_DATA) public data: ProjectDialogData
  ) {
    this.isEdit = data.mode === 'edit';

    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(255)]],
      description: [''],
      // status добавим ниже, только для edit
    });

    if (this.isEdit && data.project) {
      this.form.patchValue({
        name: data.project.name,
        description: data.project.description ?? '',
      });
      this.form.addControl(
        'status',
        this.fb.control<ProjectStatus>(data.project.status, { nonNullable: true, validators: [Validators.required] })
      );
    }
  }

  submit(): void {
    if (this.form.invalid) return;

    if (this.isEdit && this.data.project) {
      const val = this.form.value as any;
      const body: ProjectUpdateRequest = {
        name: (val.name ?? '').trim(),
        description: (val.description ?? '') || null,
        status: val.status as ProjectStatus,
      };
      this.service.update(this.data.project.id, body).subscribe({
        next: () => this.ref.close(true),
        error: () => this.ref.close(false),
      });
      return;
    }

    const val = this.form.value as any;
    const body: ProjectCreateRequest = {
      name: (val.name ?? '').trim(),
      description: (val.description ?? '') || null,
    };
    this.service.create(body).subscribe({
      next: () => this.ref.close(true),
      error: () => this.ref.close(false),
    });
  }

  cancel(): void {
    this.ref.close();
  }
}

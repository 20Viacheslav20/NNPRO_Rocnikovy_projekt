import { Component, Inject, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ProjectService } from '../../../core/services/project.service';
import { Project } from '../models/project.model';
import { ProjectRequest } from '../models/project-request.model';
import { ProjectStatus } from '../models/project-status.enum';
import { MaterialModules } from '../../../material.module'

type DialogMode = 'create' | 'edit';

interface DialogData {
    mode: DialogMode;
    project?: Project;
}

@Component({
    selector: 'app-project-dialog',
    standalone: true,
    imports: [
        CommonModule,
        ReactiveFormsModule,
        MaterialModules
    ],
    templateUrl: './project-dialog.component.html',
    styleUrls: ['./project-dialog.component.scss']
})
export class ProjectDialogComponent implements OnInit {
    private readonly fb = inject(FormBuilder);
    private readonly service = inject(ProjectService);
    private readonly snack = inject(MatSnackBar);

    form!: FormGroup;
    ProjectStatus = ProjectStatus;

    constructor(
        @Inject(MAT_DIALOG_DATA) public data: DialogData,
        private dialogRef: MatDialogRef<ProjectDialogComponent>
    ) { }

    ngOnInit(): void {
        this.form = this.fb.group({
            name: [
                this.data.project?.name ?? '',
                [Validators.required, Validators.minLength(1), Validators.maxLength(120)]
            ],
            description: [
                this.data.project?.description ?? ''
            ],
            ...(this.data.mode === 'edit'
                ? {
                    status: [
                        this.data.project?.status ?? ProjectStatus.ACTIVE,
                        [Validators.required]
                    ]
                }
                : {})
        });
    }

    saving = false;

    save(): void {
        if (this.form.invalid) return;
        const value = this.form.value as ProjectRequest;
        this.saving = true;

        if (this.data.mode === 'create') {
            this.service.create(value).subscribe({
                next: (created) => { this.saving = false; this.dialogRef.close(created); },
                error: (err) => { this.saving = false; this.snack.open(err, 'Close', { duration: 3000 }); }
            });
        } else {
            const id = this.data.project!.id;
            this.service.update(id, value).subscribe({
                next: (updated) => { this.saving = false; this.dialogRef.close(updated); },
                error: (err) => { this.saving = false; this.snack.open(err, 'Close', { duration: 3000 }); }
            });
        }
    }

    cancel(): void {
        this.dialogRef.close();
    }

}
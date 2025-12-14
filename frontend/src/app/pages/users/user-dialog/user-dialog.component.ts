import { Component, Inject, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { UserService } from '../../../core/services/user.service';
import { User } from '../../../shared/models/user.model';
import { MaterialModules } from '../../../material.module'
import { UserRequest } from '../model/user-request.model';

@Component({
    selector: 'app-user-dialog',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, MaterialModules],
    templateUrl: './user-dialog.component.html',
    styleUrls: ['./user-dialog.component.scss']
})
export class UserDialogComponent implements OnInit {

    private readonly fb = inject(FormBuilder);
    private readonly service = inject(UserService);
    private readonly snack = inject(MatSnackBar);

    form!: FormGroup;

    constructor(
        @Inject(MAT_DIALOG_DATA) public data: { mode: 'create' | 'edit', user?: User },
        private dialogRef: MatDialogRef<UserDialogComponent>
    ) { }

    ngOnInit(): void {
        this.form = this.fb.group({
            email: [this.data.user?.username ?? '', [Validators.required, Validators.email]],
            name: [this.data.user?.name ?? '', Validators.required],
            surname: [this.data.user?.surname ?? '', Validators.required],
            role: [this.data.user?.role ?? 'USER', Validators.required],
            password: [
                '',
                this.data.mode === 'create'
                    ? [Validators.required, Validators.minLength(6), Validators.maxLength(200)]
                    : [Validators.minLength(6), Validators.maxLength(200)]
            ]
        });
    }

    save() {
        if (this.form.invalid) return;

        const value = this.form.value as UserRequest;

        if (this.data.mode === 'create') {
            this.service.create(value).subscribe({
                next: created => this.dialogRef.close(created),
                error: err => this.snack.open('Failed to create', 'Close', { duration: 3000 })
            });
        } else {
            this.service.update(this.data.user!.id, value).subscribe({
                next: updated => this.dialogRef.close(updated),
                error: err => this.snack.open('Update failed', 'Close', { duration: 3000 })
            });
        }
    }

    cancel() {
        this.dialogRef.close();
    }
}

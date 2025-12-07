import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MaterialModules } from '../../../material.module'
import { Ticket, TicketRequest } from '../ticket.models';
import { User } from '../../../shared/models/user.model'
import { UserService } from '../../../core/services/user.service'

type DialogMode = 'create' | 'edit';

interface DialogData {
    mode: DialogMode;
    ticket?: Ticket;
}

@Component({
    selector: 'app-ticket-dialog',
    standalone: true,
    imports: [
        CommonModule, ReactiveFormsModule, MaterialModules
    ],
    templateUrl: './ticket-dialog.component.html',
    styleUrls: ['./ticket-dialog.component.scss']
})
export class TicketDialogComponent {

    form: FormGroup;
    mode: DialogMode = 'create';
    users: User[] = [];

    constructor(
        private fb: FormBuilder,
        private userService: UserService,
        private ref: MatDialogRef<TicketDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public data: DialogData
    ) {
        this.mode = data.mode;
        this.form = this.fb.group({
            name: [data.ticket?.name ?? '', [Validators.required]],
            description: [data.ticket?.description ?? '', [Validators.required]],
            type: [data.ticket?.type ?? 'bug', [Validators.required]],
            priority: [data.ticket?.priority ?? 'med', [Validators.required]],
            state: [data.ticket?.state ?? 'open'],
            assigneeId: [data.ticket?.assigneeId ?? null]
        });

        this.userService.getAll().subscribe(users => {
            this.users = users;
        });
    }

    cancel(): void {
        this.ref.close();
    }

    save(): void {
        if (this.form.invalid) return;
        const value = this.form.value as TicketRequest;

        if (this.mode === 'create' && !value.state) delete value.state;
        this.ref.close(value);
    }
}

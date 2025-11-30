import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { Ticket, TicketRequest } from '../ticket.models';
import { TicketTypePipe } from '../../../shared/pipes/ticketType.pipe';
import { TicketStatePipe } from '../../../shared/pipes/ticketState.pipe';
import { TicketPriorityPipe } from '../../../shared/pipes/ticketPriority.pipe';

type DialogMode = 'create' | 'edit';

interface DialogData {
    mode: DialogMode;
    ticket?: Ticket;
}

@Component({
    selector: 'app-ticket-dialog',
    standalone: true,
    imports: [
        CommonModule, MatDialogModule, ReactiveFormsModule,
        MatFormFieldModule, MatSelectModule, MatInputModule, MatButtonModule,
    ],
    templateUrl: './ticket-dialog.component.html',
    styleUrls: ['./ticket-dialog.component.scss']
})
export class TicketDialogComponent {

    form: FormGroup;
    mode: DialogMode = 'create';

    constructor(
        private fb: FormBuilder,
        private ref: MatDialogRef<TicketDialogComponent>,
        @Inject(MAT_DIALOG_DATA) public data: DialogData
    ) {
        this.mode = data.mode;
        this.form = this.fb.group({
            name: [data.ticket?.name ?? '', [Validators.required]],
            type: [data.ticket?.type ?? 'bug', [Validators.required]],
            priority: [data.ticket?.priority ?? 'med', [Validators.required]],
            state: [data.ticket?.state ?? 'open'],
            assigneeId: [data.ticket?.assigneeId ?? null]
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

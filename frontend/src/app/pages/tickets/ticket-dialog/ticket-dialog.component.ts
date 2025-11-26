import { Component, Inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, FormGroup } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogTitle, MatDialogRef } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatOptionModule } from '@angular/material/core';

import {
  TicketCreateRequest,
  TicketPriority,
  TicketType,
  TicketState,
  Ticket,
  TicketUpdateRequest,
} from '../models/ticket.models';

export interface TicketDialogData {
  mode: 'create' | 'edit';
  ticket?: Ticket;
}

@Component({
  selector: 'app-ticket-dialog',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule,
    MatDialogTitle, MatDialogContent, MatDialogActions,
    MatFormFieldModule, MatInputModule, MatSelectModule, MatOptionModule, MatButtonModule
  ],
  templateUrl: './ticket-dialog.component.html',
  styleUrls: ['./ticket-dialog.component.scss'],
})
export class TicketDialogComponent {
  readonly types: TicketType[] = ['BUG', 'FEATURE', 'TASK'];
  readonly priorities: TicketPriority[] = ['LOW', 'MEDIUM', 'HIGH'];
  readonly states: TicketState[] = ['OPEN', 'IN_PROGRESS', 'DONE'];

  isEdit = false;

  // ВАЖНО: инициализируем форму в конструкторе, а не в поле класса
  form!: FormGroup;

  constructor(
    private readonly fb: FormBuilder,
    private readonly ref: MatDialogRef<TicketDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: TicketDialogData
  ) {
    this.isEdit = data.mode === 'edit';

    this.form = this.fb.group({
      name: ['', [Validators.required, Validators.maxLength(255)]],
      type: ['TASK' as TicketType, Validators.required],
      priority: ['MEDIUM' as TicketPriority, Validators.required],
      description: [''],
      // state добавим ниже для edit
    });

    if (this.isEdit && data.ticket) {
      this.form.patchValue({
        name: data.ticket.name,
        type: data.ticket.type,
        priority: data.ticket.priority,
        description: data.ticket.description ?? '',
      });
      this.form.addControl(
        'state',
        this.fb.control<TicketState>(data.ticket.state, { nonNullable: true, validators: [Validators.required] })
      );
    }
  }

  submit(): void {
    if (this.form.invalid) return;

    if (this.isEdit) {
      const value = this.form.value as any;
      const payload: TicketUpdateRequest = {
        name: (value.name ?? '').trim(),
        type: value.type as TicketType,
        priority: value.priority as TicketPriority,
        state: value.state as TicketState,
        description: (value.description ?? '') || null,
      };
      this.ref.close(payload);
      return;
    }

    const value = this.form.value as any;
    const payload: TicketCreateRequest = {
      name: (value.name ?? '').trim(),
      type: value.type as TicketType,
      priority: value.priority as TicketPriority,
      description: (value.description ?? '') || null,
    };
    this.ref.close(payload);
  }

  cancel(): void {
    this.ref.close();
  }
}

import { Component, Input, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { MaterialModules } from '../../../material.module';
import { TicketComment } from '../ticket-comment.model';
import { TicketCommentService } from '../../../core/services/ticket-comment.service';

@Component({
    selector: 'app-ticket-comments',
    standalone: true,
    imports: [
        CommonModule,
        FormsModule,
        MaterialModules
    ],
    templateUrl: './ticket-comments.component.html',
    styleUrls: ['./ticket-comments.component.scss']
})
export class TicketCommentsComponent implements OnInit {

    @Input() ticketId!: string;
    @Input() projectId!: string;

    comments: TicketComment[] = [];
    newText = '';

    editingId: string | null = null;
    editingText = '';

    loading = false;

    constructor(
        private service: TicketCommentService,
        private authService: AuthService
    ) { }

    ngOnInit(): void {
        this.load();
    }

    load(): void {
        if (!this.ticketId) return;

        this.loading = true;
        this.service.list(this.ticketId, this.projectId).subscribe({
            next: res => {
                this.comments = res;
                this.loading = false;
            },
            error: () => this.loading = false
        });
    }

    create(): void {
        if (!this.newText.trim()) return;

        this.service.create(this.ticketId, {
            ticketId: this.ticketId,
            text: this.newText
        }, this.projectId).subscribe({
            next: () => {
                this.newText = '';
                this.load();
            }
        });
    }

    startEdit(c: TicketComment): void {
        this.editingId = c.id;
        this.editingText = c.text;
    }

    cancelEdit(): void {
        this.editingId = null;
        this.editingText = '';
    }

    saveEdit(c: TicketComment): void {
        this.service.update(this.ticketId, c.id, {
            ticketId: this.ticketId,
            text: this.editingText
        }, this.projectId).subscribe({
            next: () => {
                this.cancelEdit();
                this.load();
            }
        });
    }

    remove(c: TicketComment): void {
        if (!confirm('Delete comment?')) return;

        this.service.delete(this.ticketId, c.id, this.projectId).subscribe({
            next: () => this.load()
        });
    }

    isMine(c: TicketComment): boolean {
        const userId = this.authService.currentUser()?.userId;
        return c.commentAuthorId === userId;
    }
}

import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatIconModule } from '@angular/material/icon';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { Router } from '@angular/router';

import { ProjectService } from '../../core/services/project.service';
import { Project } from '../projects/models/project.model';
import { ProjectStatus } from '../projects/models/project-status.enum';
import { ProjectDialogComponent } from '../projects/components/project-dialog/project-dialog.component';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatIconModule,
    MatButtonModule,
    MatDialogModule,
    MatToolbarModule,
    MatCardModule,
    MatSnackBarModule,
    MatProgressBarModule
  ],
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {
  private readonly projectService = inject(ProjectService);
  private readonly dialog = inject(MatDialog);
  private readonly snack = inject(MatSnackBar);
  private readonly router = inject(Router);

  displayedColumns = ['id', 'name', 'description', 'status', 'open', 'actions'];

  data = signal<Project[]>([]);
  loading = signal<boolean>(false);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.projectService.getAll().subscribe({
      next: (projects) => {
        this.data.set(projects);
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        this.snack.open(
          this.extractMessage(err) ?? 'Failed to load projects',
          'Close',
          { duration: 3000 }
        );
      }
    });
  }

  create(): void {
    const ref = this.dialog.open(ProjectDialogComponent, {
      width: '520px',
      data: { mode: 'create' }
    });

    ref.afterClosed().subscribe((created: Project | undefined) => {
      if (created) {
        this.snack.open('Project created', 'Close', { duration: 2000 });
        this.load();
      }
    });
  }

  edit(project: Project): void {
    const ref = this.dialog.open(ProjectDialogComponent, {
      width: '520px',
      data: { mode: 'edit', project }
    });

    ref.afterClosed().subscribe((updated: Project | undefined) => {
      if (updated) {
        this.snack.open('Project updated', 'Close', { duration: 2000 });
        this.load();
      }
    });
  }

  remove(project: Project): void {
    if (!confirm(`Delete project "${project.name}"?`)) return;

    this.projectService.delete(project.id).subscribe({
      next: () => {
        this.snack.open('Project deleted', 'Close', { duration: 2000 });
        this.load();
      },
      error: (err) => {
        this.snack.open(
          this.extractMessage(err) ?? 'Delete failed',
          'Close',
          { duration: 3000 }
        );
      }
    });
  }

  openProject(project: Project): void {
    this.router.navigate(['/projects', project.id, 'tickets']);
  }

  statusChipClass(status: ProjectStatus): string {
    return status === ProjectStatus.ACTIVE
      ? 'chip chip--active'
      : 'chip chip--archived';
  }

  private extractMessage(err: any): string | undefined {
    if (typeof err?.error === 'string') return err.error;
    if (typeof err?.error?.error === 'string') return err.error.error;
    if (typeof err?.error?.message === 'string') return err.error.message;
    return undefined;
  }
}

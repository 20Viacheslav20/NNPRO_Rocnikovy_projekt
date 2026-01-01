import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ProjectService } from '../../../core/services/project.service';
import { Project } from '../models/project.model';
import { ProjectDialogComponent } from '../project-dialog/project-dialog.component';
import { MaterialModules } from '../../../material.module'
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { ViewChild } from '@angular/core';
import { DateTimePipe } from '../../../shared/pipes/date-time.pipe';

@Component({
  selector: 'app-projects-page',
  standalone: true,
  imports: [
    CommonModule,
    MaterialModules,
    DateTimePipe
  ],
  templateUrl: './projects-page.component.html',
  styleUrls: ['./projects-page.component.scss']
})
export class ProjectsPageComponent implements OnInit {
  private readonly projectService = inject(ProjectService);
  private readonly dialog = inject(MatDialog);
  private readonly snack = inject(MatSnackBar);
  private readonly router = inject(Router);

  displayedColumns = ['name', 'description', 'status', 'owner', 'createdAt', 'actions'];
  statusFilter = signal<string>(''); // ACTIVE | ARCHIVED | '' (all)

  data = signal<Project[]>([]);
  loading = signal<boolean>(false);

  dataSource = new MatTableDataSource<Project>([]);

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  ngOnInit(): void {
    this.load();

    this.dataSource.filterPredicate = (project: Project, filter: string) => {
      const data = JSON.parse(filter);

      const nameMatch = project.name.toLowerCase().includes(data.name);
      const statusMatch =
        data.status === '' || project.status === data.status;

      return nameMatch && statusMatch;
    };
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;


  }

  applyStatusFilter(value: string) {
    this.statusFilter.set(value);
    this.dataSource.filter = JSON.stringify({
      name: '',
      status: value,
    });
  }

  applyNameFilter(value: string) {
    this.dataSource.filter = JSON.stringify({
      name: value.trim().toLowerCase(),
      status: this.statusFilter(),
    });
  }

  load(): void {
    this.loading.set(true);

    this.projectService.getAll().subscribe({
      next: (projects) => {
        projects = projects.sort(
          (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
        );
        this.data.set(projects);
        this.dataSource.data = projects;
        this.loading.set(false);
      },
      error: (err) => {
        this.loading.set(false);
        const msg = this.extractMessage(err) ?? 'Unknown error';

        this.snack.open(msg, 'Close', { duration: 3000 });
      }
    });
  }

  create(): void {
    const ref = this.dialog.open(ProjectDialogComponent, {
      width: '720px',
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
      width: '720px',
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

  private extractMessage(err: any): string | undefined {

    if (typeof err?.error === 'string') {
      return err.error;
    }

    if (typeof err?.error?.error === 'string') {
      return err.error.error;
    }

    if (typeof err?.error?.message === 'string') {
      return err.error.message;
    }

    if (typeof err?.message === 'string') {
      return err.message;
    }

    return undefined;
  }

}

import { Component, inject, OnInit, signal, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MaterialModules } from '../../../material.module'
import { MatDialog } from '@angular/material/dialog';
import { UserService } from '../../../core/services/user.service';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableDataSource } from '@angular/material/table';
import { User } from '../../../shared/models/user.model';
import { MatPaginator } from '@angular/material/paginator';
import { UserDialogComponent } from '../user-dialog/user-dialog.component';

@Component({
  selector: 'app-users-page',
  standalone: true,
  imports: [CommonModule, MaterialModules],
  templateUrl: './users-page.component.html',
  styleUrls: ['./users-page.component.scss']
})
export class UsersPageComponent implements OnInit {

  private readonly service = inject(UserService);
  private readonly dialog = inject(MatDialog);
  private readonly snack = inject(MatSnackBar);

  displayedColumns = ['username', 'name', 'surname', 'role', 'actions'];
  loading = signal(false);
  data = signal<User[]>([]);

  dataSource = new MatTableDataSource<User>([]);

  @ViewChild(MatPaginator) paginator!: MatPaginator;

  ngOnInit(): void {
    this.load();
  }

  ngAfterViewInit() {
    this.dataSource.paginator = this.paginator;
  }

  load() {
    this.loading.set(true);
    this.service.getAll().subscribe({
      next: users => {
        const formattedUsers = users.map(user => ({
          ...user,
          role: this.formatRole(user.role)
        }));

        this.data.set(formattedUsers);
        this.dataSource.data = formattedUsers;
        this.loading.set(false);
      },
      error: err => {
        this.loading.set(false);
        this.snack.open('Failed to load users', 'Close', { duration: 3000 });
      }
    });
  }

  create() {
    const ref = this.dialog.open(UserDialogComponent, { width: '600px', data: { mode: 'create' } });

    ref.afterClosed().subscribe(res => {
      if (res) this.load();
    });
  }

  edit(user: User) {
    const ref = this.dialog.open(UserDialogComponent, {
      width: '600px',
      data: { mode: 'edit', user }
    });

    ref.afterClosed().subscribe(res => {
      if (res) this.load();
    });
  }

  remove(user: User) {
    if (!confirm(`Delete ${user.username}?`)) return;

    this.service.delete(user.id).subscribe({
      next: () => {
        this.snack.open('User deleted', 'Close', { duration: 2000 });
        this.load();
      },
      error: () => this.snack.open('Failed to delete', 'Close', { duration: 2000 })
    });
  }

  formatRole(role: string): string {
    return role
      .toLowerCase()
      .split('_')
      .map(word => word.charAt(0).toUpperCase() + word.slice(1))
      .join(' ');
  }

  block(user: User) {
    if (!confirm(`Block user ${user.username}?`)) return;
    this.service.blockUser(user.id).subscribe({
      next: () => {
        this.snack.open(`${user.username} blocked`, 'Close', { duration: 2000 });
        this.load();
      },
      error: () => this.snack.open(`Failed to block ${user.username}`, 'Close', { duration: 2000 })
    });
  }

  unblock(user: User) {
    if (!confirm(`Unblock user ${user.username}?`)) return;
    this.service.unblockUser(user.id).subscribe({
      next: () => {
        this.snack.open(`${user.username} unblocked`, 'Close', { duration: 2000 });
        this.load();
      },
      error: () => this.snack.open(`Failed to unblock ${user.username}`, 'Close', { duration: 2000 })
    });
  }

}

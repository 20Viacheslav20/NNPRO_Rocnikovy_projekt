import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {
  username = '';
  name = '';
  surname = '';
  email = '';
  password = '';
  error = '';

  constructor(private auth: AuthService, private router: Router) { }

  onRegister() {
    const request = {
      username: this.username,
      name: this.name,
      surname: this.surname,
      email: this.email,
      password: this.password
    };

    this.auth.register(request).subscribe({
      next: () => this.router.navigateByUrl('/login'),
      error: err => {
        this.error = "Registration failed";
      }
    });
  }
}

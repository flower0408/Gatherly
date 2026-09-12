import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthenticationService } from '../services/authentication.service';
import { Login } from '../model/login.model';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {

  form: FormGroup;
  error: string | null = null;

  constructor(
    private fb: FormBuilder,
    private authenticationService: AuthenticationService,
    private router: Router
  ) {
    this.form = this.fb.group({
      username: [null, Validators.required],
      password: [null, Validators.required]
    });
  }

  submit(): void {
    this.error = null;

    const auth: Login = new Login();
    auth.username = this.form.value.username;
    auth.password = this.form.value.password;

    this.authenticationService.login(auth).subscribe({
      next: (result) => {
        localStorage.setItem('user', JSON.stringify(result));
        this.router.navigate(['events']).then(() => window.location.reload());
      },
      error: (response: HttpErrorResponse) => {
        // Uz 403 server kaze i zasto: nalog je nepotvrdjen ili je blokiran.
        // 401 znaci da kredencijali ne valjaju i tu se razlog namerno ne otkriva.
        if (response.status === 403) {
          this.error = typeof response.error === 'string' && response.error.length > 0
            ? response.error
            : 'This account cannot be used to sign in.';
        } else {
          this.error = 'Username or password is incorrect.';
        }
      }
    });
  }
}

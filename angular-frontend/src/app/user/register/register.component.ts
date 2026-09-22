import { Component, ChangeDetectionStrategy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { AuthenticationService } from '../services/authentication.service';
import { Register } from '../model/register.model';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.css'],
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false
})
export class RegisterComponent {

  form: FormGroup;
  error: string | null = null;
  registeredEmail: string | null = null;

  constructor(
    private fb: FormBuilder,
    private authenticationService: AuthenticationService
  ) {
    this.form = this.fb.group({
      username: [null, Validators.required],
      password: [null, [Validators.required, Validators.minLength(15)]],
      email: [null, [Validators.required, Validators.email]],
      firstName: [null, Validators.required],
      lastName: [null, Validators.required]
    });
  }

  submit(): void {
    this.error = null;

    const auth: Register = new Register();
    auth.username = this.form.value.username;
    auth.password = this.form.value.password;
    auth.email = this.form.value.email;
    auth.firstName = this.form.value.firstName;
    auth.lastName = this.form.value.lastName;

    this.authenticationService.register(auth).subscribe({
      next: () => {
        // Nalog jos nije upotrebljiv, korisnik mora da potvrdi mejl
        this.registeredEmail = auth.email;
      },
      error: (response: HttpErrorResponse) => {
        if (response.status === 406) {
          this.error = 'That username or email is already taken.';
        } else {
          this.error = this.textOf(response);
        }
      }
    });
  }

  // Server salje ili obican tekst (npr. iz PasswordPolicy) ili mapu poruka po polju
  private textOf(response: HttpErrorResponse): string {
    if (typeof response.error === 'string' && response.error.length > 0) {
      return response.error;
    }
    if (response.error && typeof response.error === 'object') {
      const messages = Object.values(response.error).filter((m) => typeof m === 'string');
      if (messages.length > 0) {
        return messages.join(' ');
      }
    }
    return 'Registration failed, please check the form.';
  }
}

import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AuthenticationService } from '../services/authentication.service';

// Link iz mejla vodi na backend, ali ista potvrda moze da se odradi i ovde,
// da bi korisnik ostao u aplikaciji umesto da vidi go tekst
@Component({
  selector: 'app-verify',
  templateUrl: './verify.component.html',
  styleUrls: ['./verify.component.css'],
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false
})
export class VerifyComponent implements OnInit {

  message: string | null = null;
  success = false;
  done = false;

  constructor(
    private route: ActivatedRoute,
    private authenticationService: AuthenticationService
  ) {}

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');

    if (!token) {
      this.message = 'This link is missing its confirmation code.';
      this.done = true;
      return;
    }

    this.authenticationService.verify(token).subscribe({
      next: (result) => {
        this.message = result;
        this.success = true;
        this.done = true;
      },
      error: (response) => {
        this.message = response.error || 'This confirmation link is invalid or has already been used.';
        this.done = true;
      }
    });
  }
}

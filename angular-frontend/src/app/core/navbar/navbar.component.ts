import { Component, ChangeDetectionStrategy } from '@angular/core';
import { Router } from '@angular/router';
import { AuthenticationService } from '../../user/services/authentication.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css'],
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false
})
export class NavbarComponent {

  constructor(
    public auth: AuthenticationService,
    private router: Router
  ) {}

  logout(): void {
    this.auth.logout();
    this.router.navigate(['events']);
  }
}

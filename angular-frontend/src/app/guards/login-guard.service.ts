import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthenticationService } from '../user/services/authentication.service';

// Prijavljenog korisnika vraca sa stranice za prijavu
@Injectable({
  providedIn: 'root'
})
export class LoginGuardService implements CanActivate {

  constructor(
    public auth: AuthenticationService,
    public router: Router
  ) {}

  canActivate(): boolean {
    if (this.auth.isLoggedIn()) {
      this.router.navigate(['events']);
      return false;
    }
    return true;
  }
}

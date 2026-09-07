import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router } from '@angular/router';
import { AuthenticationService } from '../user/services/authentication.service';

// Propusta samo uloge navedene u 'expectedRoles' na ruti
@Injectable({
  providedIn: 'root'
})
export class RoleGuardService implements CanActivate {

  constructor(
    public auth: AuthenticationService,
    public router: Router
  ) {}

  canActivate(route: ActivatedRouteSnapshot): boolean {
    const expectedRoles: string = route.data['expectedRoles'];
    const role = this.auth.getRole();

    if (!role) {
      this.router.navigate(['users/login']);
      return false;
    }

    if (expectedRoles.split('|').indexOf(role) === -1) {
      this.router.navigate(['events']);
      return false;
    }

    return true;
  }
}

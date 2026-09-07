import { Routes } from '@angular/router';

import { LoginComponent } from '../user/login/login.component';
import { RegisterComponent } from '../user/register/register.component';
import { VerifyComponent } from '../user/verify/verify.component';
import { EventListComponent } from '../event/event-list/event-list.component';
import { LoginGuardService } from '../guards/login-guard.service';

export const routes: Routes = [
  // Pocetna je javna, gost vidi dogadjaje bez prijave
  { path: 'events', component: EventListComponent, title: 'Events - Gatherly' },

  { path: 'users/login', component: LoginComponent, canActivate: [LoginGuardService], title: 'Sign in - Gatherly' },
  { path: 'users/register', component: RegisterComponent, canActivate: [LoginGuardService], title: 'Sign up - Gatherly' },
  { path: 'users/verify', component: VerifyComponent, title: 'Account activation - Gatherly' },

  { path: '', pathMatch: 'full', redirectTo: 'events' },
  { path: '**', redirectTo: 'events' }
];

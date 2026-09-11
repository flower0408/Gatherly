import { Routes } from '@angular/router';

import { LoginComponent } from '../user/login/login.component';
import { RegisterComponent } from '../user/register/register.component';
import { VerifyComponent } from '../user/verify/verify.component';
import { EventListComponent } from '../event/event-list/event-list.component';
import { EventDetailComponent } from '../event/event-detail/event-detail.component';
import { AddEventComponent } from '../event/add-event/add-event.component';
import { EditEventComponent } from '../event/edit-event/edit-event.component';
import { EventRegistrationsComponent } from '../event/event-registrations/event-registrations.component';
import { MyRegistrationsComponent } from '../event/my-registrations/my-registrations.component';
import { CommunityListComponent } from '../community/community-list/community-list.component';
import { CommunityDetailComponent } from '../community/community-detail/community-detail.component';
import { AddCommunityComponent } from '../community/add-community/add-community.component';
import { EditCommunityComponent } from '../community/edit-community/edit-community.component';
import { BanListComponent } from '../banned/ban-list/ban-list.component';
import { ReportListComponent } from '../report/report-list/report-list.component';
import { LoginGuardService } from '../guards/login-guard.service';
import { RoleGuardService } from '../guards/role-guard.service';

export const routes: Routes = [
  // Pocetna je javna, gost vidi dogadjaje bez prijave
  { path: 'events', component: EventListComponent, title: 'Events - Gatherly' },
  { path: 'events/new', component: AddEventComponent, canActivate: [RoleGuardService],
    data: { expectedRoles: 'USER|ADMIN' }, title: 'New event - Gatherly' },
  { path: 'events/:id', component: EventDetailComponent, title: 'Event - Gatherly' },
  { path: 'events/:id/edit', component: EditEventComponent, canActivate: [RoleGuardService],
    data: { expectedRoles: 'USER|ADMIN' }, title: 'Edit event - Gatherly' },
  { path: 'events/:id/registrations', component: EventRegistrationsComponent, canActivate: [RoleGuardService],
    data: { expectedRoles: 'USER|ADMIN' }, title: 'Registrations - Gatherly' },

  { path: 'registrations', component: MyRegistrationsComponent, canActivate: [RoleGuardService],
    data: { expectedRoles: 'USER|ADMIN' }, title: 'My registrations - Gatherly' },

  { path: 'communities', component: CommunityListComponent, title: 'Communities - Gatherly' },
  { path: 'communities/new', component: AddCommunityComponent, canActivate: [RoleGuardService],
    data: { expectedRoles: 'USER|ADMIN' }, title: 'New community - Gatherly' },
  { path: 'communities/:id', component: CommunityDetailComponent, title: 'Community - Gatherly' },
  { path: 'communities/:id/edit', component: EditCommunityComponent, canActivate: [RoleGuardService],
    data: { expectedRoles: 'USER|ADMIN' }, title: 'Edit community - Gatherly' },

  { path: 'reports', component: ReportListComponent, canActivate: [RoleGuardService],
    data: { expectedRoles: 'ADMIN' }, title: 'Reports - Gatherly' },
  { path: 'communities/:id/reports', component: ReportListComponent, canActivate: [RoleGuardService],
    data: { expectedRoles: 'USER|ADMIN' }, title: 'Community reports - Gatherly' },
  { path: 'bans', component: BanListComponent, canActivate: [RoleGuardService],
    data: { expectedRoles: 'ADMIN' }, title: 'Banned people - Gatherly' },
  { path: 'communities/:id/bans', component: BanListComponent, canActivate: [RoleGuardService],
    data: { expectedRoles: 'USER|ADMIN' }, title: 'Banned from community - Gatherly' },

  { path: 'users/login', component: LoginComponent, canActivate: [LoginGuardService], title: 'Sign in - Gatherly' },
  { path: 'users/register', component: RegisterComponent, canActivate: [LoginGuardService], title: 'Sign up - Gatherly' },
  { path: 'users/verify', component: VerifyComponent, title: 'Account activation - Gatherly' },

  { path: '', pathMatch: 'full', redirectTo: 'events' },
  { path: '**', redirectTo: 'events' }
];

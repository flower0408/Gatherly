import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';

import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing/app-routing.module';
import { TokenInterceptor } from './core/token.interceptor';
import { NavbarComponent } from './core/navbar/navbar.component';
import { FooterComponent } from './core/footer/footer.component';
import { LoginComponent } from './user/login/login.component';
import { RegisterComponent } from './user/register/register.component';
import { VerifyComponent } from './user/verify/verify.component';
import { EventListComponent } from './event/event-list/event-list.component';
import { EventCardComponent } from './event/event-card/event-card.component';
import { EventDetailComponent } from './event/event-detail/event-detail.component';
import { AddEventComponent } from './event/add-event/add-event.component';
import { EditEventComponent } from './event/edit-event/edit-event.component';
import { EventRegistrationsComponent } from './event/event-registrations/event-registrations.component';
import { MyRegistrationsComponent } from './event/my-registrations/my-registrations.component';
import { CommunityCardComponent } from './community/community-card/community-card.component';
import { CommunityListComponent } from './community/community-list/community-list.component';
import { CommunityDetailComponent } from './community/community-detail/community-detail.component';
import { AddCommunityComponent } from './community/add-community/add-community.component';
import { EditCommunityComponent } from './community/edit-community/edit-community.component';

@NgModule({
  declarations: [
    AppComponent,
    NavbarComponent,
    FooterComponent,
    LoginComponent,
    RegisterComponent,
    VerifyComponent,
    EventListComponent,
    EventCardComponent,
    EventDetailComponent,
    AddEventComponent,
    EditEventComponent,
    MyRegistrationsComponent,
    EventRegistrationsComponent,
    CommunityCardComponent,
    CommunityListComponent,
    CommunityDetailComponent,
    AddCommunityComponent,
    EditCommunityComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule,
    ReactiveFormsModule,
    HttpClientModule
  ],
  providers: [
    { provide: HTTP_INTERCEPTORS, useClass: TokenInterceptor, multi: true }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }

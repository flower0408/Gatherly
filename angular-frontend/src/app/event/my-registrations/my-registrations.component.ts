import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { RegistrationService } from '../services/registration.service';
import { EventService } from '../services/event.service';
import { Registration } from '../model/registration.model';
import { Event } from '../model/event.model';

@Component({
  selector: 'app-my-registrations',
  templateUrl: './my-registrations.component.html',
  styleUrls: ['./my-registrations.component.css'],
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false
})
export class MyRegistrationsComponent implements OnInit {

  registrations: Registration[] = [];
  loading = true;
  error: string | null = null;

  // Prijava nosi samo id dogadjaja, pa se dogadjaji dovlace posebno i pamte po id-u
  events: { [id: number]: Event } = {};

  constructor(
    private registrationService: RegistrationService,
    private eventService: EventService
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;

    this.registrationService.getMine().subscribe({
      next: (result) => {
        this.registrations = result;
        this.loading = false;
        result.forEach((registration) => this.loadEvent(registration.forEventId));
      },
      error: () => {
        this.registrations = [];
        this.loading = false;
      }
    });
  }

  private loadEvent(id: number): void {
    if (this.events[id]) {
      return;
    }
    this.eventService.getOne(id).subscribe({
      next: (event) => this.events[id] = event,
      error: () => {}
    });
  }

  cancel(registration: Registration): void {
    if (!confirm('Cancel your registration for this event?')) {
      return;
    }
    this.error = null;

    this.registrationService.cancel(registration.id).subscribe({
      next: () => this.load(),
      error: (response: HttpErrorResponse) => {
        this.error = typeof response.error === 'string' && response.error.length > 0
          ? response.error
          : 'That registration could not be cancelled.';
      }
    });
  }

  eventOf(registration: Registration): Event | undefined {
    return this.events[registration.forEventId];
  }

  canCancel(registration: Registration): boolean {
    const event = this.events[registration.forEventId];
    if (!event || new Date(event.startsAt) < new Date()) {
      return false;
    }
    return registration.status === 'PENDING'
      || registration.status === 'WAITLISTED'
      || registration.status === 'ACCEPTED';
  }

  label(status: string): string {
    switch (status) {
      case 'PENDING': return 'Waiting for the organizer';
      case 'WAITLISTED': return 'On the waiting list';
      case 'ACCEPTED': return 'Going';
      case 'REJECTED': return 'Not accepted';
      case 'CANCELLED': return 'Cancelled';
      case 'ATTENDED': return 'Attended';
      case 'NO_SHOW': return 'No-show';
      default: return status;
    }
  }

  badge(status: string): string {
    switch (status) {
      case 'ACCEPTED':
      case 'ATTENDED': return 'text-bg-success';
      case 'REJECTED':
      case 'NO_SHOW': return 'text-bg-danger';
      case 'WAITLISTED': return 'text-bg-warning';
      default: return 'text-bg-secondary';
    }
  }
}

import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { RegistrationService } from '../services/registration.service';
import { EventService } from '../services/event.service';
import { Registration } from '../model/registration.model';
import { Event } from '../model/event.model';

@Component({
  selector: 'app-event-registrations',
  templateUrl: './event-registrations.component.html',
  styleUrls: ['./event-registrations.component.css'],
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false
})
export class EventRegistrationsComponent implements OnInit {

  event: Event | null = null;
  registrations: Registration[] = [];
  loading = true;
  forbidden = false;
  message: string | null = null;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private registrationService: RegistrationService,
    private eventService: EventService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.eventService.getOne(id).subscribe({
      next: (result) => this.event = result,
      error: () => this.event = null
    });

    this.load(id);
  }

  private load(eventId: number): void {
    this.loading = true;

    this.registrationService.getForEvent(eventId).subscribe({
      next: (result) => {
        this.registrations = result;
        this.loading = false;
      },
      error: (response: HttpErrorResponse) => {
        // Rutu vidi samo organizator dogadjaja i administrator
        this.forbidden = response.status === 403;
        this.registrations = [];
        this.loading = false;
      }
    });
  }

  // Prijave se grupisu po stanju, da organizator prvo vidi ono sto ceka na njega
  get waiting(): Registration[] {
    return this.registrations.filter((r) => r.status === 'PENDING');
  }

  get waitlisted(): Registration[] {
    return this.registrations.filter((r) => r.status === 'WAITLISTED');
  }

  // Pre pocetka dogadjaja se odvajaju potvrdjeni od resenih. Posle pocetka svi koji su
  // imali mesto stoje u istom spisku, pa red ne skace u drugu grupu cim se upise dolazak
  // i organizator moze mirno da prolazi kroz spisak odozgo nadole.
  get going(): Registration[] {
    if (this.hasStarted) {
      return this.registrations.filter((r) => r.status === 'ACCEPTED'
        || r.status === 'ATTENDED' || r.status === 'NO_SHOW');
    }
    return this.registrations.filter((r) => r.status === 'ACCEPTED');
  }

  get goingTitle(): string {
    return this.hasStarted ? 'Who turned up' : 'Going';
  }

  get settled(): Registration[] {
    if (this.hasStarted) {
      return this.registrations.filter((r) => r.status === 'REJECTED' || r.status === 'CANCELLED');
    }
    return this.registrations.filter((r) => r.status === 'REJECTED'
      || r.status === 'CANCELLED' || r.status === 'ATTENDED' || r.status === 'NO_SHOW');
  }

  get hasStarted(): boolean {
    return this.event !== null && new Date(this.event.startsAt) < new Date();
  }

  get freeSpots(): number {
    return this.event ? this.event.capacity - this.event.takenSpots : 0;
  }

  accept(registration: Registration): void {
    this.run(this.registrationService.accept(registration.id));
  }

  reject(registration: Registration): void {
    this.run(this.registrationService.reject(registration.id));
  }

  markAttended(registration: Registration): void {
    this.run(this.registrationService.markAttended(registration.id));
  }

  markNoShow(registration: Registration): void {
    this.run(this.registrationService.markNoShow(registration.id));
  }

  // Posle svake odluke se lista i dogadjaj ucitavaju ponovo, jer se broj mesta i lista cekanja menjaju
  private run(request: Observable<Registration>): void {
    this.message = null;
    this.error = null;

    request.subscribe({
      next: () => {
        this.message = 'Saved.';
        if (this.event) {
          this.load(this.event.id);
          this.eventService.getOne(this.event.id).subscribe({
            next: (result) => this.event = result,
            error: () => {}
          });
        }
      },
      error: (response: HttpErrorResponse) => {
        this.error = typeof response.error === 'string' && response.error.length > 0
          ? response.error
          : 'That action could not be completed.';
      }
    });
  }

  label(status: string): string {
    switch (status) {
      case 'PENDING': return 'Waiting';
      case 'WAITLISTED': return 'Waiting list';
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

  // Skor pouzdanosti govori koliko se cesto ucesnik zaista pojavio, pa se boji po vrednosti
  reliabilityClass(score: number | null): string {
    if (score === null) {
      return 'text-body-secondary';
    }
    if (score >= 80) {
      return 'text-success';
    }
    return score >= 50 ? 'text-warning' : 'text-danger';
  }
}

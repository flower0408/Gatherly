import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EventService } from '../services/event.service';
import { CommunityService } from '../../community/services/community.service';
import { UserService } from '../../user/services/user.service';
import { RegistrationService } from '../services/registration.service';
import { AuthenticationService } from '../../user/services/authentication.service';
import { Event } from '../model/event.model';
import { Community } from '../../community/model/community.model';
import { Registration } from '../model/registration.model';
import { User } from '../../user/model/user.model';
import { categoryLabel } from '../model/event-category.model';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-event-detail',
  templateUrl: './event-detail.component.html',
  styleUrls: ['./event-detail.component.css']
})
export class EventDetailComponent implements OnInit {

  event: Event | null = null;
  community: Community | null = null;
  loading = true;
  notFound = false;
  canManage = false;
  myId: number | null = null;
  categoryLabel = categoryLabel;
  participants: User[] = [];
  // Dogadjaj van zajednice je otvoren za razgovor, u zajednici pisu njeni clanovi
  canWrite = true;
  registration: Registration | null = null;
  message: string | null = null;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private eventService: EventService,
    private communityService: CommunityService,
    private userService: UserService,
    private registrationService: RegistrationService,
    public auth: AuthenticationService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.eventService.getOne(id).subscribe({
      next: (result) => {
        this.event = result;
        this.loading = false;
        // Dogadjaj ne mora da pripada zajednici
        if (result.belongsToCommunityId) {
          this.loadCommunity(result.belongsToCommunityId);
          this.canWrite = false;
          this.checkMembership(result.belongsToCommunityId);
        }
        this.checkOwnership(result);
        this.loadMyRegistration(result.id);
        this.loadParticipants(result.id);
      },
      error: () => {
        this.notFound = true;
        this.loading = false;
      }
    });
  }

  // Izmena i brisanje se nude samo tvorcu dogadjaja i administratoru
  private checkOwnership(event: Event): void {
    if (!this.auth.isLoggedIn()) {
      return;
    }
    this.userService.whoAmI().subscribe({
      next: (me) => {
        this.myId = me.id;
        this.canManage = this.auth.isAdmin() || me.id === event.createdByUserId;
      },
      error: () => this.canManage = this.auth.isAdmin()
    });
  }

  // Prijava prijavljenog korisnika za ovaj dogadjaj, ako je ima
  private loadMyRegistration(eventId: number): void {
    if (!this.auth.isLoggedIn()) {
      return;
    }
    this.registrationService.getMine().subscribe({
      next: (result) => {
        this.registration = result.find((r) => r.forEventId === eventId && r.status !== 'CANCELLED') || null;
      },
      error: () => this.registration = null
    });
  }

  // Spisak onih koji dolaze vidi samo prijavljen korisnik
  private loadParticipants(eventId: number): void {
    if (!this.auth.isLoggedIn()) {
      return;
    }
    this.registrationService.getParticipants(eventId).subscribe({
      next: (result) => this.participants = result,
      error: () => this.participants = []
    });
  }

  register(): void {
    if (!this.event) {
      return;
    }
    this.message = null;
    this.error = null;

    this.registrationService.register(this.event.id).subscribe({
      next: (created) => {
        this.registration = created;
        this.message = created.status === 'WAITLISTED'
          ? 'The event is full, so you are on the waiting list. You will be notified if a spot opens up.'
          : 'Your request has been sent, the organizer will confirm it.';
        this.reload();
        if (this.event) {
          this.loadParticipants(this.event.id);
        }
        if (this.event && this.event.belongsToCommunityId) {
          this.checkMembership(this.event.belongsToCommunityId);
        }
      },
      error: (response: HttpErrorResponse) => this.error = this.textOf(response)
    });
  }

  cancelRegistration(): void {
    if (!this.registration || !confirm('Cancel your registration for this event?')) {
      return;
    }
    this.message = null;
    this.error = null;

    this.registrationService.cancel(this.registration.id).subscribe({
      next: () => {
        this.registration = null;
        this.message = 'Your registration has been cancelled.';
        this.reload();
        if (this.event) {
          this.loadParticipants(this.event.id);
        }
      },
      error: (response: HttpErrorResponse) => this.error = this.textOf(response)
    });
  }

  // Broj zauzetih mesta se menja posle prijave, pa se dogadjaj ucitava ponovo
  private reload(): void {
    if (!this.event) {
      return;
    }
    this.eventService.getOne(this.event.id).subscribe({
      next: (result) => this.event = result,
      error: () => {}
    });
  }

  private textOf(response: HttpErrorResponse): string {
    return typeof response.error === 'string' && response.error.length > 0
      ? response.error
      : 'That action could not be completed.';
  }

  remove(): void {
    if (!this.event || !confirm('Delete this event? Registrations and comments will be removed too.')) {
      return;
    }
    this.eventService.delete(this.event.id).subscribe({
      next: () => this.router.navigate(['/events']),
      error: () => alert('This event could not be deleted.')
    });
  }

  // Clanstvo se proverava tek kad znamo i zajednicu i prijavljenog korisnika
  private checkMembership(communityId: number): void {
    if (!this.auth.isLoggedIn()) {
      return;
    }
    if (this.auth.isAdmin()) {
      this.canWrite = true;
      return;
    }
    this.userService.whoAmI().subscribe({
      next: (me) => {
        this.communityService.getMembers(communityId).subscribe({
          next: (members) => this.canWrite = members.some((m) => m.id === me.id),
          error: () => this.canWrite = false
        });
      },
      error: () => this.canWrite = false
    });
  }
  
  private loadCommunity(id: number): void {
    this.communityService.getOne(id).subscribe({
      next: (result) => this.community = result,
      error: () => this.community = null
    });
  }

  get coverImage(): string | null {
    if (!this.event || !this.event.images || this.event.images.length === 0) {
      return null;
    }
    return this.event.images[0].path;
  }

  get freeSpots(): number {
    if (!this.event) {
      return 0;
    }
    return this.event.capacity - this.event.takenSpots;
  }

  get isFull(): boolean {
    return this.freeSpots <= 0;
  }

  // Statusi se korisniku prikazuju recima, a ne kao vrednosti iz baze
  get statusLabel(): string {
    switch (this.registration?.status) {
      case 'PENDING': return 'Waiting for the organizer';
      case 'WAITLISTED': return 'On the waiting list';
      case 'ACCEPTED': return 'You are going';
      case 'REJECTED': return 'Not accepted';
      case 'ATTENDED': return 'You attended';
      case 'NO_SHOW': return 'Marked as no-show';
      default: return '';
    }
  }

  get statusClass(): string {
    switch (this.registration?.status) {
      case 'ACCEPTED':
      case 'ATTENDED': return 'text-bg-success';
      case 'REJECTED':
      case 'NO_SHOW': return 'text-bg-danger';
      case 'WAITLISTED': return 'text-bg-warning';
      default: return 'text-bg-secondary';
    }
  }

  get hasStarted(): boolean {
    if (!this.event) {
      return false;
    }
    return new Date(this.event.startsAt) < new Date();
  }

  get hasFinished(): boolean {
    if (!this.event) {
      return false;
    }
    return new Date(this.event.endsAt) < new Date();
  }
}

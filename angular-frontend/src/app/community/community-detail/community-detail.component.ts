import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { CommunityService } from '../services/community.service';
import { EventService } from '../../event/services/event.service';
import { AuthenticationService } from '../../user/services/authentication.service';
import { UserService } from '../../user/services/user.service';
import { BanService } from '../../banned/services/ban.service';
import { Community } from '../model/community.model';
import { Event } from '../../event/model/event.model';
import { User } from '../../user/model/user.model';

@Component({
  selector: 'app-community-detail',
  templateUrl: './community-detail.component.html',
  styleUrls: ['./community-detail.component.css'],
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false
})
export class CommunityDetailComponent implements OnInit {

  community: Community | null = null;
  events: Event[] = [];
  members: User[] = [];
  organizers: User[] = [];
  loading = true;
  notFound = false;
  order = 'asc';
  canManage = false;
  isMember = false;
  message: string | null = null;
  error: string | null = null;
  suspending = false;
  suspendedReason = '';

  // Prijavljeni korisnik, treba nam da znamo da li je vec clan i da li je organizator
  private me: User | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private communityService: CommunityService,
    private eventService: EventService,
    private userService: UserService,
    private banService: BanService,
    public auth: AuthenticationService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.communityService.getOne(id).subscribe({
      next: (result) => {
        this.community = result;
        this.loading = false;
        this.loadEvents();
        this.loadPeople();
      },
      error: () => {
        this.notFound = true;
        this.loading = false;
      }
    });
  }

  loadEvents(): void {
    if (!this.community) {
      return;
    }
    this.eventService.getForCommunitySorted(this.community.id, this.order).subscribe({
      next: (result) => this.events = result,
      error: () => this.events = []
    });
  }

  sortBy(order: string): void {
    if (this.order === order) {
      return;
    }
    this.order = order;
    this.loadEvents();
  }

  loadPeople(): void {
    if (!this.community) {
      return;
    }
    const id = this.community.id;

    this.communityService.getOrganizers(id).subscribe({
      next: (result) => {
        this.organizers = result;
        this.updateRights();
      },
      error: () => this.organizers = []
    });

    this.communityService.getMembers(id).subscribe({
      next: (result) => {
        this.members = result;
        this.updateRights();
      },
      error: () => this.members = []
    });

    if (this.auth.isLoggedIn() && this.me === null) {
      this.userService.whoAmI().subscribe({
        next: (me) => {
          this.me = me;
          this.updateRights();
        },
        error: () => this.me = null
      });
    }
  }

  // Izmena i dodavanje organizatora se nude organizatorima i administratoru
  private updateRights(): void {
    if (!this.auth.isLoggedIn()) {
      return;
    }
    this.isMember = this.me !== null && this.members.some((m) => m.id === this.me!.id);
    this.canManage = this.auth.isAdmin()
      || (this.me !== null && this.organizers.some((o) => o.id === this.me!.id));
  }

  isOrganizer(user: User): boolean {
    return this.organizers.some((o) => o.id === user.id);
  }

  isMe(user: User): boolean {
    return this.me !== null && this.me.id === user.id;
  }

  join(): void {
    if (!this.community) {
      return;
    }
    this.clearNotices();
    this.communityService.join(this.community.id).subscribe({
      next: (response) => {
        this.message = response;
        this.loadPeople();
      },
      error: (response: HttpErrorResponse) => this.error = this.textOf(response)
    });
  }

  leave(): void {
    if (!this.community) {
      return;
    }
    this.clearNotices();
    this.communityService.leave(this.community.id).subscribe({
      next: (response) => {
        this.message = response;
        this.loadPeople();
      },
      error: (response: HttpErrorResponse) => this.error = this.textOf(response)
    });
  }

  makeOrganizer(user: User): void {
    if (!this.community) {
      return;
    }
    this.clearNotices();
    this.communityService.addOrganizer(this.community.id, user.id).subscribe({
      next: (response) => {
        this.message = response;
        this.loadPeople();
      },
      error: (response: HttpErrorResponse) => this.error = this.textOf(response)
    });
  }

  removeOrganizer(user: User): void {
    const question = this.isMe(user)
      ? 'Step down as an organizer of this community?'
      : 'Remove ' + user.username + ' from the organizers?';
    if (!this.community || !confirm(question)) {
      return;
    }
    this.clearNotices();
    this.communityService.removeOrganizer(this.community.id, user.id).subscribe({
      next: (response) => {
        this.message = response;
        this.loadPeople();
      },
      error: (response: HttpErrorResponse) => this.error = this.textOf(response)
    });
  }

  // Brisanje zajednice odnosi i njene dogadjaje, pa upozorenje mora to da kaze
  remove(): void {
    if (!this.community || !confirm('Delete ' + this.community.name
        + '? Its events will be removed too, along with their registrations and comments.')) {
      return;
    }
    this.clearNotices();
    this.communityService.delete(this.community.id).subscribe({
      next: () => this.router.navigate(['/communities']),
      error: (response: HttpErrorResponse) => this.error = this.textOf(response)
    });
  }

  startSuspending(): void {
    this.suspending = true;
    this.suspendedReason = '';
    this.clearNotices();
  }

  cancelSuspending(): void {
    this.suspending = false;
    this.suspendedReason = '';
  }

  // Suspendovana zajednica ostaje bez organizatora i u njoj se vise nista ne dogadja
  suspend(): void {
    if (!this.community || !this.suspendedReason.trim()) {
      return;
    }
    this.clearNotices();
    this.communityService.suspend(this.community.id, this.suspendedReason).subscribe({
      next: (updated) => {
        this.community = updated;
        this.suspending = false;
        this.message = 'This community has been suspended.';
        this.loadPeople();
      },
      error: (response: HttpErrorResponse) => this.error = this.textOf(response)
    });
  }

  // Blokada u zajednici je posao njenog organizatora, blokiran korisnik prestaje da bude clan
  banMember(user: User): void {
    if (!this.community || !confirm('Ban ' + user.username + ' from this community?')) {
      return;
    }
    this.clearNotices();
    this.banService.banFromCommunity(this.community.id, user.id).subscribe({
      next: () => {
        this.message = user.username + ' has been banned from this community.';
        this.loadPeople();
      },
      error: (response: HttpErrorResponse) => this.error = this.textOf(response)
    });
  }
  
  private clearNotices(): void {
    this.message = null;
    this.error = null;
  }

  private textOf(response: HttpErrorResponse): string {
    return typeof response.error === 'string' && response.error.length > 0
      ? response.error
      : 'That action could not be completed.';
  }
}

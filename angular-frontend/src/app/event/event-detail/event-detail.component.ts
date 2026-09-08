import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { EventService } from '../services/event.service';
import { CommunityService } from '../../community/services/community.service';
import { UserService } from '../../user/services/user.service';
import { AuthenticationService } from '../../user/services/authentication.service';
import { Event } from '../model/event.model';
import { Community } from '../../community/model/community.model';

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

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private eventService: EventService,
    private communityService: CommunityService,
    private userService: UserService,
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
        }
        this.checkOwnership(result);
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
    if (this.auth.isAdmin()) {
      this.canManage = true;
      return;
    }
    this.userService.whoAmI().subscribe({
      next: (me) => this.canManage = me.id === event.createdByUserId,
      error: () => this.canManage = false
    });
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

  get hasStarted(): boolean {
    if (!this.event) {
      return false;
    }
    return new Date(this.event.startsAt) < new Date();
  }
}

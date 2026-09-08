import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CommunityService } from '../services/community.service';
import { EventService } from '../../event/services/event.service';
import { AuthenticationService } from '../../user/services/authentication.service';
import { UserService } from '../../user/services/user.service';
import { Community } from '../model/community.model';
import { Event } from '../../event/model/event.model';

@Component({
  selector: 'app-community-detail',
  templateUrl: './community-detail.component.html',
  styleUrls: ['./community-detail.component.css']
})
export class CommunityDetailComponent implements OnInit {

  community: Community | null = null;
  events: Event[] = [];
  loading = true;
  notFound = false;
  order = 'asc';
  canManage = false;

  constructor(
    private route: ActivatedRoute,
    private communityService: CommunityService,
    private eventService: EventService,
    private userService: UserService,
    public auth: AuthenticationService
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    this.communityService.getOne(id).subscribe({
      next: (result) => {
        this.community = result;
        this.loading = false;
        this.loadEvents();
        this.checkOrganizer(result.id);
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

  // Izmena zajednice se nudi njenim organizatorima i administratoru
  private checkOrganizer(communityId: number): void {
    if (!this.auth.isLoggedIn()) {
      return;
    }
    if (this.auth.isAdmin()) {
      this.canManage = true;
      return;
    }
    this.userService.whoAmI().subscribe({
      next: (me) => {
        this.communityService.getOrganizers(communityId).subscribe({
          next: (organizers) => this.canManage = organizers.some((o) => o.id === me.id),
          error: () => this.canManage = false
        });
      },
      error: () => this.canManage = false
    });
  }
}

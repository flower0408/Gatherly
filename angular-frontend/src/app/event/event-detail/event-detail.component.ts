import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { EventService } from '../services/event.service';
import { CommunityService } from '../../community/services/community.service';
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

  constructor(
    private route: ActivatedRoute,
    private eventService: EventService,
    private communityService: CommunityService
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
      },
      error: () => {
        this.notFound = true;
        this.loading = false;
      }
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

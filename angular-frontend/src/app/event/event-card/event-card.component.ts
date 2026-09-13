import { Component, Input } from '@angular/core';
import { Event } from '../model/event.model';
import { categoryLabel } from '../model/event-category.model';

// Kartica dogadjaja, koristi se na listi i kasnije na stranici zajednice
@Component({
  selector: 'app-event-card',
  templateUrl: './event-card.component.html',
  styleUrls: ['./event-card.component.css']
})
export class EventCardComponent {

  categoryLabel = categoryLabel;

  @Input() event!: Event;

  get coverImage(): string | null {
    if (!this.event.images || this.event.images.length === 0) {
      return null;
    }
    return this.event.images[0].path;
  }

  get freeSpots(): number {
    return this.event.capacity - this.event.takenSpots;
  }

  get isFull(): boolean {
    return this.freeSpots <= 0;
  }
}

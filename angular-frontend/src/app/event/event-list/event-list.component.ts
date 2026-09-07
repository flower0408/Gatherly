import { Component, OnInit } from '@angular/core';
import { EventService } from '../services/event.service';
import { Event } from '../model/event.model';

@Component({
  selector: 'app-event-list',
  templateUrl: './event-list.component.html',
  styleUrls: ['./event-list.component.css']
})
export class EventListComponent implements OnInit {

  events: Event[] = [];
  loading = true;
  showPast = false;

  constructor(private eventService: EventService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;

    const request = this.showPast ? this.eventService.getAll() : this.eventService.getUpcoming();

    request.subscribe({
      next: (result) => {
        this.events = result;
        this.loading = false;
      },
      error: () => {
        this.events = [];
        this.loading = false;
      }
    });
  }

  togglePast(value: boolean): void {
    if (this.showPast === value) {
      return;
    }
    this.showPast = value;
    this.load();
  }
}

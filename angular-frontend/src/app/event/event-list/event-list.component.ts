import { Component, OnInit } from '@angular/core';
import { EventService } from '../services/event.service';
import { Event } from '../model/event.model';
import { EVENT_CATEGORIES } from '../model/event-category.model';

@Component({
  selector: 'app-event-list',
  templateUrl: './event-list.component.html',
  styleUrls: ['./event-list.component.css']
})
export class EventListComponent implements OnInit {

  events: Event[] = [];
  loading = true;
  showPast = false;

  // Filteri pretrage, prazna vrednost znaci da se taj filter ne primenjuje
  categories = EVENT_CATEGORIES;
  term = '';
  category = '';
  from = '';
  to = '';

  constructor(private eventService: EventService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;

    this.eventService.search(this.term, this.category, this.from, this.to, !this.showPast).subscribe({
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

  clearFilters(): void {
    this.term = '';
    this.category = '';
    this.from = '';
    this.to = '';
    this.load();
  }

  get hasFilters(): boolean {
    return this.term !== '' || this.category !== '' || this.from !== '' || this.to !== '';
  }
}

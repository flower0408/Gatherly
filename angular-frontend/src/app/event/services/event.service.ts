import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Event } from '../model/event.model';

@Injectable({
  providedIn: 'root'
})
export class EventService {

  constructor(private http: HttpClient) {}

  // Predstojeci dogadjaji, vidljivi i bez prijave
  getUpcoming(): Observable<Event[]> {
    return this.http.get('api/events') as Observable<Event[]>;
  }

  getAll(): Observable<Event[]> {
    return this.http.get('api/events/all') as Observable<Event[]>;
  }

  getOne(id: number): Observable<Event> {
    return this.http.get('api/events/' + id) as Observable<Event>;
  }

  getForCommunity(communityId: number): Observable<Event[]> {
    return this.http.get('api/events/community/' + communityId) as Observable<Event[]>;
  }

  getForCommunitySorted(communityId: number, order: string): Observable<Event[]> {
    return this.http.get('api/events/community/' + communityId + '/sort/' + order) as Observable<Event[]>;
  }

  // Dogadjaji koje jedan korisnik organizuje, samo oni koji tek predstoje
  getForUser(userId: number): Observable<Event[]> {
    return this.http.get('api/events/user/' + userId) as Observable<Event[]>;
  }

  getMine(): Observable<Event[]> {
    return this.http.get('api/events/my') as Observable<Event[]>;
  }

  getHomepage(): Observable<Event[]> {
    return this.http.get('api/events/homepage') as Observable<Event[]>;
  }

  create(event: Event): Observable<Event> {
    return this.http.post('api/events/add', event) as Observable<Event>;
  }

  update(id: number, event: Partial<Event>): Observable<Event> {
    return this.http.patch('api/events/edit/' + id, event) as Observable<Event>;
  }

  delete(id: number): Observable<any> {
    return this.http.delete('api/events/delete/' + id) as Observable<any>;
  }
}

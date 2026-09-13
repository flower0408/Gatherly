import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Registration } from '../model/registration.model';
import { User } from '../../user/model/user.model';

@Injectable({
  providedIn: 'root'
})
export class RegistrationService {

  constructor(private http: HttpClient) {}

  register(eventId: number): Observable<Registration> {
    return this.http.post('api/registrations/event/' + eventId, null) as Observable<Registration>;
  }

  getMine(): Observable<Registration[]> {
    return this.http.get('api/registrations/my') as Observable<Registration[]>;
  }

  getForEvent(eventId: number): Observable<Registration[]> {
    return this.http.get('api/registrations/event/' + eventId) as Observable<Registration[]>;
  }

  // Ucesnici sa potvrdjenim mestom, spisak vidi svaki prijavljen korisnik
  getParticipants(eventId: number): Observable<User[]> {
    return this.http.get('api/registrations/event/' + eventId + '/going') as Observable<User[]>;
  }

  accept(id: number): Observable<Registration> {
    return this.http.patch('api/registrations/' + id + '/accept', null) as Observable<Registration>;
  }

  reject(id: number): Observable<Registration> {
    return this.http.patch('api/registrations/' + id + '/reject', null) as Observable<Registration>;
  }

  markAttended(id: number): Observable<Registration> {
    return this.http.patch('api/registrations/' + id + '/attended', null) as Observable<Registration>;
  }

  markNoShow(id: number): Observable<Registration> {
    return this.http.patch('api/registrations/' + id + '/no-show', null) as Observable<Registration>;
  }

  cancel(id: number): Observable<any> {
    return this.http.delete('api/registrations/' + id) as Observable<any>;
  }
}

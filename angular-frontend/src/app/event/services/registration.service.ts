import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Registration } from '../model/registration.model';

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

  cancel(id: number): Observable<any> {
    return this.http.delete('api/registrations/' + id) as Observable<any>;
  }
}

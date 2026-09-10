import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ReactionService {

  constructor(private http: HttpClient) {}

  // Broj reakcija po vrsti, na primer { LIKE: 3, HEART: 1 }
  countsForEvent(eventId: number): Observable<{ [type: string]: number }> {
    return this.http.get('api/reactions/event/' + eventId) as Observable<{ [type: string]: number }>;
  }

  countsForComment(commentId: number): Observable<{ [type: string]: number }> {
    return this.http.get('api/reactions/comment/' + commentId) as Observable<{ [type: string]: number }>;
  }

  // Ponovljena ista reakcija se povlaci, pa se odgovor ne koristi nego se brojevi ucitavaju ponovo
  reactToEvent(eventId: number, reactionType: string): Observable<any> {
    return this.http.post('api/reactions/event/' + eventId, { reactionType }) as Observable<any>;
  }

  reactToComment(commentId: number, reactionType: string): Observable<any> {
    return this.http.post('api/reactions/comment/' + commentId, { reactionType }) as Observable<any>;
  }

  // Bez reakcije server vraca 204, pa telo odgovora ostaje prazno
  mineOnEvent(eventId: number): Observable<any> {
    return this.http.get('api/reactions/mine/event/' + eventId) as Observable<any>;
  }

  mineOnComment(commentId: number): Observable<any> {
    return this.http.get('api/reactions/mine/comment/' + commentId) as Observable<any>;
  }
}
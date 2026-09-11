import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Ban } from '../model/ban.model';

@Injectable({
  providedIn: 'root'
})
export class BanService {

  constructor(private http: HttpClient) {}

  // Blokade na nivou sistema, vidi ih samo administrator
  getSystemBans(): Observable<Ban[]> {
    return this.http.get('api/bans') as Observable<Ban[]>;
  }

  getCommunityBans(communityId: number): Observable<Ban[]> {
    return this.http.get('api/bans/community/' + communityId) as Observable<Ban[]>;
  }

  banFromSystem(userId: number): Observable<Ban> {
    return this.http.post('api/bans/user/' + userId, null) as Observable<Ban>;
  }

  banFromCommunity(communityId: number, userId: number): Observable<Ban> {
    return this.http.post('api/bans/community/' + communityId + '/user/' + userId, null) as Observable<Ban>;
  }

  // Odgovor je obican tekst, pa se trazi takav odgovor
  unban(id: number): Observable<string> {
    return this.http.delete('api/bans/' + id, { responseType: 'text' });
  }
}
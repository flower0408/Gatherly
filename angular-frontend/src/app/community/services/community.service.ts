import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Community } from '../model/community.model';
import { User } from '../../user/model/user.model';

@Injectable({
  providedIn: 'root'
})
export class CommunityService {

  constructor(private http: HttpClient) {}

  getAll(): Observable<Community[]> {
    return this.http.get('api/communities') as Observable<Community[]>;
  }

  getOne(id: number): Observable<Community> {
    return this.http.get('api/communities/' + id) as Observable<Community>;
  }

  getMembers(id: number): Observable<User[]> {
    return this.http.get('api/communities/members/' + id) as Observable<User[]>;
  }

  getOrganizers(id: number): Observable<User[]> {
    return this.http.get('api/communities/organizers/' + id) as Observable<User[]>;
  }

  // Zajednice kojima pripada bilo koji korisnik, za prikaz na njegovom profilu
  getForUser(userId: number): Observable<Community[]> {
    return this.http.get('api/communities/user/' + userId) as Observable<Community[]>;
  }

  // Zajednice u kojima je prijavljeni korisnik organizator, dakle sme da otvara dogadjaje
  getMineOrganizing(): Observable<Community[]> {
    return this.http.get('api/communities/my/organizing') as Observable<Community[]>;
  }

  create(community: Community): Observable<Community> {
    return this.http.post('api/communities/add', community) as Observable<Community>;
  }

  update(id: number, community: Partial<Community>): Observable<Community> {
    return this.http.patch('api/communities/edit/' + id, community) as Observable<Community>;
  }

  delete(id: number): Observable<any> {
    return this.http.delete('api/communities/delete/' + id) as Observable<any>;
  }

  // Suspenziju izrice administrator i uz nju obavezno ide razlog
  suspend(id: number, suspendedReason: string): Observable<Community> {
    return this.http.patch('api/communities/suspend/' + id, { suspendedReason }) as Observable<Community>;
  }

  // Odgovori na ove rute su obican tekst, pa se trazi takav odgovor
  join(id: number): Observable<string> {
    return this.http.post('api/communities/' + id + '/member', null, { responseType: 'text' });
  }

  leave(id: number): Observable<string> {
    return this.http.delete('api/communities/' + id + '/member', { responseType: 'text' });
  }

  addOrganizer(id: number, userId: number): Observable<string> {
    return this.http.post('api/communities/' + id + '/organizer/' + userId, null, { responseType: 'text' });
  }

  removeOrganizer(id: number, userId: number): Observable<string> {
    return this.http.delete('api/communities/delete/' + id + '/organizer/' + userId, { responseType: 'text' });
  }
}

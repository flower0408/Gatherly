import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Community } from '../model/community.model';

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

  // Zajednice ciji je prijavljeni korisnik clan
  getMine(): Observable<Community[]> {
    return this.http.get('api/communities/my') as Observable<Community[]>;
  }

  // Zajednice u kojima je prijavljeni korisnik organizator, dakle sme da otvara dogadjaje
  getMineOrganizing(): Observable<Community[]> {
    return this.http.get('api/communities/my/organizing') as Observable<Community[]>;
  }
}

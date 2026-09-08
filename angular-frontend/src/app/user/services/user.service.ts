import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User } from '../model/user.model';

@Injectable({
  providedIn: 'root'
})
export class UserService {

  constructor(private http: HttpClient) {}

  whoAmI(): Observable<User> {
    return this.http.get('api/users/whoami') as Observable<User>;
  }

  getOne(id: number): Observable<User> {
    return this.http.get('api/users/' + id) as Observable<User>;
  }
}

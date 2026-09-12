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

  update(user: Partial<User>): Observable<User> {
    return this.http.patch('api/users/edit', user) as Observable<User>;
  }

  changePassword(oldPassword: string, newPassword: string): Observable<User> {
    return this.http.post('api/users/change-password', { oldPassword, newPassword }) as Observable<User>;
  }

  // Otpremljena slika se posebnim pozivom postavlja kao profilna
  setProfileImage(path: string): Observable<any> {
    return this.http.post('api/images/profile', { path }) as Observable<any>;
  }
}

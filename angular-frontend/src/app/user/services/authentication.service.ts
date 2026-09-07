import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { JwtHelperService } from '@auth0/angular-jwt';
import { Login } from '../model/login.model';
import { Register } from '../model/register.model';
import { User } from '../model/user.model';
import { UserToken } from '../model/userToken.model';

@Injectable({
  providedIn: 'root'
})
export class AuthenticationService {

  private headers = new HttpHeaders({ 'Content-Type': 'application/json' });

  constructor(private http: HttpClient) {}

  login(auth: Login): Observable<UserToken> {
    return this.http.post('api/users/login',
      { username: auth.username, password: auth.password },
      { headers: this.headers }) as Observable<UserToken>;
  }

  register(auth: Register): Observable<User> {
    return this.http.post('api/users/signup', auth, { headers: this.headers }) as Observable<User>;
  }

  // Link iz mejla vodi na backend, ali front nudi i rucni unos tokena
  verify(token: string): Observable<string> {
    return this.http.get('api/users/verify', {
      params: { token: token },
      responseType: 'text'
    });
  }

  logout(): void {
    localStorage.removeItem('user');
  }

  isLoggedIn(): boolean {
    return localStorage.getItem('user') !== null;
  }

  // Uloga se cita iz samog tokena, isto kao u cuvaru ruta
  getRole(): string | null {
    const stored = localStorage.getItem('user');
    if (!stored) {
      return null;
    }
    const info = new JwtHelperService().decodeToken(JSON.parse(stored).accessToken);
    if (!info || !info.role) {
      return null;
    }
    return info.role.authority.replace('ROLE_', '');
  }

  getUsername(): string | null {
    const stored = localStorage.getItem('user');
    if (!stored) {
      return null;
    }
    const info = new JwtHelperService().decodeToken(JSON.parse(stored).accessToken);
    return info ? info.sub : null;
  }

  isAdmin(): boolean {
    return this.getRole() === 'ADMIN';
  }
}

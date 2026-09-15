import { TestBed } from '@angular/core/testing';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthenticationService } from './authentication.service';
import { Login } from '../model/login.model';
import { provideHttpClient, withInterceptorsFromDi, withXhr } from '@angular/common/http';

// Uloga i korisnicko ime se citaju iz samog tokena, pa se u testu upisuje token
// sa poznatim sadrzajem i proverava sta servis iz njega izvuce.
describe('AuthenticationService', () => {

  let service: AuthenticationService;
  let httpMock: HttpTestingController;

  // Token je sastavljen rucno: zaglavlje, sadrzaj i potpis razdvojeni tackom.
  // Potpis se ovde ne proverava, jer to radi server.
  function tokenFor(username: string, role: string): string {
    const header = btoa(JSON.stringify({ alg: 'HS512' }));
    const payload = btoa(JSON.stringify({ sub: username, role: { authority: 'ROLE_' + role } }));
    return header + '.' + payload + '.potpis';
  }

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthenticationService,
        provideHttpClient(withXhr(), withInterceptorsFromDi()),
        provideHttpClientTesting()
      ]
    });

    service = TestBed.inject(AuthenticationService);
    httpMock = TestBed.inject(HttpTestingController);
    localStorage.clear();
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('sends the username and password to the sign in route', () => {
    const auth = new Login();
    auth.username = 'ana';
    auth.password = 'gatherly demo 2026';

    service.login(auth).subscribe();

    const request = httpMock.expectOne('api/users/login');
    expect(request.request.method).toBe('POST');
    expect(request.request.body.username).toBe('ana');
    request.flush({ accessToken: tokenFor('ana', 'USER'), expiresIn: 3600000 });
  });

  it('says nobody is signed in when there is no token', () => {
    expect(service.isLoggedIn()).toBeFalse();
    expect(service.getUsername()).toBeNull();
    expect(service.getRole()).toBeNull();
    expect(service.isAdmin()).toBeFalse();
  });

  it('reads the username out of the token', () => {
    localStorage.setItem('user', JSON.stringify({ accessToken: tokenFor('ana', 'USER') }));

    expect(service.isLoggedIn()).toBeTrue();
    expect(service.getUsername()).toBe('ana');
  });

  it('reads the role out of the token without the prefix', () => {
    localStorage.setItem('user', JSON.stringify({ accessToken: tokenFor('ana', 'USER') }));

    expect(service.getRole()).toBe('USER');
    expect(service.isAdmin()).toBeFalse();
  });

  it('recognises an administrator', () => {
    localStorage.setItem('user', JSON.stringify({ accessToken: tokenFor('pera', 'ADMIN') }));

    expect(service.getRole()).toBe('ADMIN');
    expect(service.isAdmin()).toBeTrue();
  });

  it('forgets the token when signing out', () => {
    localStorage.setItem('user', JSON.stringify({ accessToken: tokenFor('ana', 'USER') }));

    service.logout();

    expect(service.isLoggedIn()).toBeFalse();
  });
});

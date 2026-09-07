import { Injectable } from '@angular/core';
import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';

// Umesto da svaki servis sam dodaje zaglavlje sa tokenom, presretac ga dodaje na svaki zahtev
@Injectable()
export class TokenInterceptor implements HttpInterceptor {

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const stored = localStorage.getItem('user');

    if (stored && request.url.startsWith('api/')) {
      const token = JSON.parse(stored).accessToken;
      request = request.clone({
        setHeaders: { authorization: 'Bearer ' + token }
      });
    }

    return next.handle(request);
  }
}

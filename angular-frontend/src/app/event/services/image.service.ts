import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Image } from '../model/image.model';

@Injectable({
  providedIn: 'root'
})
export class ImageService {

  constructor(private http: HttpClient) {}

  // Fajl se prvo otprema, pa se dobijena putanja salje uz dogadjaj.
  // Content-Type se namerno ne postavlja, brauzer ga sam popuni sa granicom za multipart.
  upload(file: File): Observable<Image> {
    const data = new FormData();
    data.append('file', file);

    return this.http.post('api/images/upload', data) as Observable<Image>;
  }
}

import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Report } from '../model/report.model';

@Injectable({
  providedIn: 'root'
})
export class ReportService {

  constructor(private http: HttpClient) {}

  create(report: Partial<Report>): Observable<Report> {
    return this.http.post('api/reports', report) as Observable<Report>;
  }

  // Sve prijave na sistemu, vidi ih samo administrator
  getAll(pendingOnly: boolean): Observable<Report[]> {
    return this.http.get('api/reports' + (pendingOnly ? '?pending=true' : '')) as Observable<Report[]>;
  }

  // Prijave na sadrzaj jedne zajednice, vidi ih njen organizator
  getForCommunity(communityId: number): Observable<Report[]> {
    return this.http.get('api/reports/community/' + communityId) as Observable<Report[]>;
  }

  accept(id: number): Observable<Report> {
    return this.http.patch('api/reports/' + id + '/accept', null) as Observable<Report>;
  }

  reject(id: number): Observable<Report> {
    return this.http.patch('api/reports/' + id + '/reject', null) as Observable<Report>;
  }
}
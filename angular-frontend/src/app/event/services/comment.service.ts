import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Comment } from '../model/comment.model';

@Injectable({
  providedIn: 'root'
})
export class CommentService {

  constructor(private http: HttpClient) {}

  // sortBy je 'date', 'like', 'dislike' ili 'heart', a order 'asc' ili 'desc'
  getForEvent(eventId: number, sortBy: string, order: string): Observable<Comment[]> {
    return this.http.get('api/comments/event/' + eventId + '/sort/' + sortBy + '/' + order) as Observable<Comment[]>;
  }

  getReplies(id: number): Observable<Comment[]> {
    return this.http.get('api/comments/' + id + '/replies') as Observable<Comment[]>;
  }

  add(eventId: number, comment: Partial<Comment>): Observable<Comment> {
    return this.http.post('api/comments/event/' + eventId, comment) as Observable<Comment>;
  }

  reply(id: number, comment: Partial<Comment>): Observable<Comment> {
    return this.http.post('api/comments/' + id + '/reply', comment) as Observable<Comment>;
  }

  update(id: number, comment: Partial<Comment>): Observable<Comment> {
    return this.http.patch('api/comments/' + id, comment) as Observable<Comment>;
  }

  delete(id: number): Observable<any> {
    return this.http.delete('api/comments/' + id) as Observable<any>;
  }
}
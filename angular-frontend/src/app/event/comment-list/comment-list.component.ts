import { Component, Input, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { CommentService } from '../services/comment.service';
import { Comment } from '../model/comment.model';

@Component({
  selector: 'app-comment-list',
  templateUrl: './comment-list.component.html',
  styleUrls: ['./comment-list.component.css'],
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false
})
export class CommentListComponent implements OnInit {

  @Input() eventId!: number;
  @Input() myId: number | null = null;
  @Input() canModerate = false;
  // Razgovor uz dogadjaj zajednice vode njeni clanovi
  @Input() canWrite = true;
  @Input() communityId: number | null = null;

  comments: Comment[] = [];
  loading = true;
  order = 'desc';
  sortField = 'date';
  draft = '';
  error: string | null = null;
  // Isto ogranicenje kao na backend-u, da korisnik unapred vidi koliko prostora ima
  readonly maxLength = 200;

  constructor(private commentService: CommentService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;

    this.commentService.getForEvent(this.eventId, this.sortField, this.order).subscribe({
      next: (result) => {
        this.comments = result;
        this.loading = false;
      },
      error: () => {
        this.comments = [];
        this.loading = false;
      }
    });
  }

  // Po datumu se bira smer, a po reakciji se uvek prikazuju najpopularniji prvi
  sortBy(field: string, order: string): void {
    if (this.sortField === field && this.order === order) {
      return;
    }
    this.sortField = field;
    this.order = order;
    this.load();
  }

  isSorted(field: string, order: string): boolean {
    return this.sortField === field && this.order === order;
  }

  submit(): void {
    if (!this.draft.trim()) {
      return;
    }
    this.error = null;

    this.commentService.add(this.eventId, {
      text: this.draft
    }).subscribe({
      next: () => {
        this.draft = '';
        this.load();
      },
      error: (response: HttpErrorResponse) => {
        this.error = typeof response.error === 'string' && response.error.length > 0
          ? response.error
          : 'Your comment could not be posted.';
      }
    });
  }
}
import { Component, Input, OnInit } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { CommentService } from '../services/comment.service';
import { Comment } from '../model/comment.model';

@Component({
  selector: 'app-comment-list',
  templateUrl: './comment-list.component.html',
  styleUrls: ['./comment-list.component.css']
})
export class CommentListComponent implements OnInit {

  @Input() eventId!: number;
  @Input() myId: number | null = null;
  @Input() canModerate = false;

  comments: Comment[] = [];
  loading = true;
  order = 'desc';
  draft = '';
  error: string | null = null;

  constructor(private commentService: CommentService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;

    this.commentService.getForEvent(this.eventId, 'date', this.order).subscribe({
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

  sortBy(order: string): void {
    if (this.order === order) {
      return;
    }
    this.order = order;
    this.load();
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
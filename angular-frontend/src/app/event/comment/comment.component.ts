import { Component, EventEmitter, Input, OnInit, Output, ChangeDetectionStrategy } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { CommentService } from '../services/comment.service';
import { Comment } from '../model/comment.model';

@Component({
  selector: 'app-comment',
  templateUrl: './comment.component.html',
  styleUrls: ['./comment.component.css'],
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false
})
export class CommentComponent implements OnInit {

  @Input() comment!: Comment;
  @Input() eventId!: number;
  // Id prijavljenog korisnika, po njemu se zna da li se nudi izmena
  @Input() myId: number | null = null;
  // Autor dogadjaja i administrator mogu da obrisu tudji komentar
  @Input() canModerate = false;
  @Input() canWrite = true;
  @Output() removed = new EventEmitter<void>();

  replies: Comment[] = [];
  showReplies = false;
  replying = false;
  editing = false;
  draft = '';
  error: string | null = null;

  constructor(private commentService: CommentService) {}

  ngOnInit(): void {
    this.loadReplies();
  }

  loadReplies(): void {
    this.commentService.getReplies(this.comment.id).subscribe({
      next: (result) => {
        this.replies = result;
        // Odgovori se otvaraju sami samo ako ih ima, da nit ne bude skrivena
        this.showReplies = result.length > 0;
      },
      error: () => this.replies = []
    });
  }

  get isMine(): boolean {
    return this.myId !== null && this.myId === this.comment.belongsToUserId;
  }

  startReply(): void {
    this.replying = true;
    this.editing = false;
    this.draft = '';
    this.error = null;
  }

  startEdit(): void {
    this.editing = true;
    this.replying = false;
    this.draft = this.comment.text;
    this.error = null;
  }

  cancel(): void {
    this.replying = false;
    this.editing = false;
    this.draft = '';
    this.error = null;
  }

  submitReply(): void {
    if (!this.draft.trim()) {
      return;
    }
    this.error = null;

    this.commentService.reply(this.comment.id, {
      text: this.draft
    }).subscribe({
      next: () => {
        this.cancel();
        this.loadReplies();
      },
      error: (response: HttpErrorResponse) => this.error = this.textOf(response)
    });
  }

  submitEdit(): void {
    if (!this.draft.trim()) {
      return;
    }
    this.error = null;

    this.commentService.update(this.comment.id, { text: this.draft }).subscribe({
      next: (updated) => {
        this.comment.text = updated.text;
        this.cancel();
      },
      error: (response: HttpErrorResponse) => this.error = this.textOf(response)
    });
  }

  remove(): void {
    if (!confirm('Delete this comment? Its replies will be removed too.')) {
      return;
    }
    this.error = null;

    this.commentService.delete(this.comment.id).subscribe({
      next: () => this.removed.emit(),
      error: (response: HttpErrorResponse) => this.error = this.textOf(response)
    });
  }

  private textOf(response: HttpErrorResponse): string {
    return typeof response.error === 'string' && response.error.length > 0
      ? response.error
      : 'That action could not be completed.';
  }
}
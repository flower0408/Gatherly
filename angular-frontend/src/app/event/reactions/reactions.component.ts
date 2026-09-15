import { Component, Input, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { ReactionService } from '../services/reaction.service';

@Component({
  selector: 'app-reactions',
  templateUrl: './reactions.component.html',
  styleUrls: ['./reactions.component.css'],
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false
})
export class ReactionsComponent implements OnInit {

  // Reakcije stoje ili uz dogadjaj ili uz komentar, zavisno od toga sta je prosledjeno
  @Input() eventId: number | null = null;
  @Input() commentId: number | null = null;
  @Input() canReact = false;

  counts: { [type: string]: number } = {};
  mine: string | null = null;
  types = ['LIKE', 'DISLIKE', 'HEART'];

  constructor(private reactionService: ReactionService) {}

  ngOnInit(): void {
    this.loadCounts();
    if (this.canReact) {
      this.loadMine();
    }
  }

  private loadCounts(): void {
    const request = this.eventId !== null
      ? this.reactionService.countsForEvent(this.eventId)
      : this.reactionService.countsForComment(this.commentId!);

    request.subscribe({
      next: (result) => this.counts = result || {},
      error: () => this.counts = {}
    });
  }

  private loadMine(): void {
    const request = this.eventId !== null
      ? this.reactionService.mineOnEvent(this.eventId)
      : this.reactionService.mineOnComment(this.commentId!);

    request.subscribe({
      next: (result) => this.mine = result ? result.reactionType : null,
      error: () => this.mine = null
    });
  }

  react(type: string): void {
    if (!this.canReact) {
      return;
    }
    const request = this.eventId !== null
      ? this.reactionService.reactToEvent(this.eventId, type)
      : this.reactionService.reactToComment(this.commentId!, type);

    request.subscribe({
      next: () => {
        // Klik na istu reakciju je povlaci, klik na drugu je menja
        this.mine = this.mine === type ? null : type;
        this.loadCounts();
      },
      error: () => {}
    });
  }

  countOf(type: string): number {
    return this.counts[type] || 0;
  }

  icon(type: string): string {
    switch (type) {
      case 'LIKE': return '👍';
      case 'DISLIKE': return '👎';
      default: return '❤️';
    }
  }

  title(type: string): string {
    switch (type) {
      case 'LIKE': return 'Like';
      case 'DISLIKE': return 'Dislike';
      default: return 'Love it';
    }
  }
}
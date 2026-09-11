import { Component, Input } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { ReportService } from '../services/report.service';

@Component({
  selector: 'app-report-button',
  templateUrl: './report-button.component.html',
  styleUrls: ['./report-button.component.css']
})
export class ReportButtonComponent {

  // Prijava se odnosi na tacno jednu stvar, pa se prosledjuje samo jedan od ova tri
  @Input() eventId: number | null = null;
  @Input() commentId: number | null = null;
  @Input() userId: number | null = null;
  @Input() canReport = false;

  open = false;
  reason = 'BREAKS_RULES';
  sending = false;
  message: string | null = null;
  error: string | null = null;

  // Vrednosti su iste kao u ReportReason na serveru, uz citljive nazive
  reasons = [
    { value: 'BREAKS_RULES', label: 'Breaks the rules' },
    { value: 'HARASSMENT', label: 'Harassment' },
    { value: 'HATE', label: 'Hate speech' },
    { value: 'SHARING_PERSONAL_INFORMATION', label: 'Sharing personal information' },
    { value: 'IMPERSONATION', label: 'Impersonation' },
    { value: 'COPYRIGHT_VIOLATION', label: 'Copyright violation' },
    { value: 'TRADEMARK_VIOLATION', label: 'Trademark violation' },
    { value: 'SPAM', label: 'Spam' },
    { value: 'SCAM_OR_MISLEADING', label: 'Scam or misleading event' },
    { value: 'DANGEROUS_OR_ILLEGAL', label: 'Dangerous or illegal activity' },
    { value: 'SELF_HARM_OR_SUICIDE', label: 'Self-harm or suicide' },
    { value: 'OTHER', label: 'Something else' }
  ];

  constructor(private reportService: ReportService) {}

  toggle(): void {
    this.open = !this.open;
    this.error = null;
  }

  submit(): void {
    this.sending = true;
    this.error = null;

    this.reportService.create({
      reason: this.reason,
      onEventId: this.eventId,
      onCommentId: this.commentId,
      onUserId: this.userId
    }).subscribe({
      next: () => {
        this.open = false;
        this.sending = false;
        this.message = 'Thanks, this has been reported.';
      },
      error: (response: HttpErrorResponse) => {
        this.error = typeof response.error === 'string' && response.error.length > 0
          ? response.error
          : 'This could not be reported.';
        this.sending = false;
      }
    });
  }
}
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ReportService } from '../services/report.service';
import { CommunityService } from '../../community/services/community.service';
import { Report } from '../model/report.model';
import { Community } from '../../community/model/community.model';

@Component({
  selector: 'app-report-list',
  templateUrl: './report-list.component.html',
  styleUrls: ['./report-list.component.css']
})
export class ReportListComponent implements OnInit {

  reports: Report[] = [];
  loading = true;
  forbidden = false;
  pendingOnly = true;
  message: string | null = null;
  error: string | null = null;

  // Kada je zadata zajednica, prikazuju se prijave na njen sadrzaj, inace prijave na celom sistemu
  communityId: number | null = null;
  community: Community | null = null;

  constructor(
    private route: ActivatedRoute,
    private reportService: ReportService,
    private communityService: CommunityService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    this.communityId = id ? Number(id) : null;

    if (this.communityId !== null) {
      this.communityService.getOne(this.communityId).subscribe({
        next: (result) => this.community = result,
        error: () => this.community = null
      });
    }

    this.load();
  }

  load(): void {
    this.loading = true;

    const request = this.communityId !== null
      ? this.reportService.getForCommunity(this.communityId)
      : this.reportService.getAll(this.pendingOnly);

    request.subscribe({
      next: (result) => {
        this.reports = result;
        this.loading = false;
      },
      error: (response: HttpErrorResponse) => {
        this.forbidden = response.status === 403;
        this.reports = [];
        this.loading = false;
      }
    });
  }

  togglePending(value: boolean): void {
    if (this.pendingOnly === value) {
      return;
    }
    this.pendingOnly = value;
    this.load();
  }

  get waiting(): Report[] {
    return this.reports.filter((r) => r.accepted === null);
  }

  get reviewed(): Report[] {
    return this.reports.filter((r) => r.accepted !== null);
  }

  accept(report: Report): void {
    if (!confirm('Accept this report? The reported content will be removed.')) {
      return;
    }
    this.run(this.reportService.accept(report.id));
  }

  reject(report: Report): void {
    this.run(this.reportService.reject(report.id));
  }

  private run(request: Observable<Report>): void {
    this.message = null;
    this.error = null;

    request.subscribe({
      next: () => {
        this.message = 'Saved.';
        this.load();
      },
      error: (response: HttpErrorResponse) => {
        this.error = typeof response.error === 'string' && response.error.length > 0
          ? response.error
          : 'That action could not be completed.';
      }
    });
  }

  // Prijava se odnosi na tacno jednu stvar, ali ta stvar je mozda vec uklonjena
  target(report: Report): string {
    if (report.onEventId !== null) {
      return 'Event';
    }
    if (report.onCommentId !== null) {
      return 'Comment';
    }
    return report.onUserId !== null ? 'User' : 'Already removed';
  }

  reasonLabel(reason: string): string {
    return reason.charAt(0) + reason.slice(1).toLowerCase().replace(/_/g, ' ');
  }
}
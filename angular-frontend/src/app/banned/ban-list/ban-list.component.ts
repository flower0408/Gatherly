import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { BanService } from '../services/ban.service';
import { CommunityService } from '../../community/services/community.service';
import { Ban } from '../model/ban.model';
import { Community } from '../../community/model/community.model';

@Component({
  selector: 'app-ban-list',
  templateUrl: './ban-list.component.html',
  styleUrls: ['./ban-list.component.css']
})
export class BanListComponent implements OnInit {

  bans: Ban[] = [];
  loading = true;
  forbidden = false;
  message: string | null = null;
  error: string | null = null;

  // Kada je zadata zajednica, prikazuju se blokade u njoj, inace one na nivou sistema
  communityId: number | null = null;
  community: Community | null = null;

  constructor(
    private route: ActivatedRoute,
    private banService: BanService,
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
      ? this.banService.getCommunityBans(this.communityId)
      : this.banService.getSystemBans();

    request.subscribe({
      next: (result) => {
        this.bans = result;
        this.loading = false;
      },
      error: (response: HttpErrorResponse) => {
        this.forbidden = response.status === 403;
        this.bans = [];
        this.loading = false;
      }
    });
  }

  unban(ban: Ban): void {
    if (!confirm('Lift the ban for ' + ban.towardsUsername + '?')) {
      return;
    }
    this.message = null;
    this.error = null;

    this.banService.unban(ban.id).subscribe({
      next: (response) => {
        this.message = response;
        this.load();
      },
      error: (response: HttpErrorResponse) => {
        this.error = typeof response.error === 'string' && response.error.length > 0
          ? response.error
          : 'The ban could not be lifted.';
      }
    });
  }
}
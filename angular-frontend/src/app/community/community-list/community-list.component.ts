import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { CommunityService } from '../services/community.service';
import { AuthenticationService } from '../../user/services/authentication.service';
import { Community } from '../model/community.model';

@Component({
  selector: 'app-community-list',
  templateUrl: './community-list.component.html',
  styleUrls: ['./community-list.component.css'],
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false
})
export class CommunityListComponent implements OnInit {

  communities: Community[] = [];
  loading = true;

  constructor(
    private communityService: CommunityService,
    public auth: AuthenticationService
  ) {}

  ngOnInit(): void {
    this.communityService.getAll().subscribe({
      next: (result) => {
        this.communities = result;
        this.loading = false;
      },
      error: () => {
        this.communities = [];
        this.loading = false;
      }
    });
  }
}

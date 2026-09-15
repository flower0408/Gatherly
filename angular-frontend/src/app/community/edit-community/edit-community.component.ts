import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { CommunityService } from '../services/community.service';

@Component({
  selector: 'app-edit-community',
  templateUrl: './edit-community.component.html',
  styleUrls: ['./edit-community.component.css'],
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false
})
export class EditCommunityComponent implements OnInit {

  form: FormGroup;
  communityId = 0;
  error: string | null = null;
  loading = true;
  saving = false;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private communityService: CommunityService,
    private router: Router
  ) {
    this.form = this.fb.group({
      name: [null, Validators.required],
      description: [null, Validators.required]
    });
  }

  ngOnInit(): void {
    this.communityId = Number(this.route.snapshot.paramMap.get('id'));

    this.communityService.getOne(this.communityId).subscribe({
      next: (community) => {
        this.form.patchValue({
          name: community.name,
          description: community.description
        });
        this.loading = false;
      },
      error: () => {
        this.error = 'This community could not be loaded.';
        this.loading = false;
      }
    });
  }

  submit(): void {
    this.error = null;
    this.saving = true;

    const changes: any = {
      name: this.form.value.name,
      description: this.form.value.description
    };

    this.communityService.update(this.communityId, changes).subscribe({
      next: () => {
        this.router.navigate(['/communities', this.communityId]);
      },
      error: (response: HttpErrorResponse) => {
        this.error = response.status === 403
          ? 'Only an organizer can edit this community.'
          : 'The changes could not be saved, please check the form.';
        this.saving = false;
      }
    });
  }
}

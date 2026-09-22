import { Component, ChangeDetectionStrategy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { CommunityService } from '../services/community.service';

@Component({
  selector: 'app-add-community',
  templateUrl: './add-community.component.html',
  styleUrls: ['./add-community.component.css'],
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false
})
export class AddCommunityComponent {

  form: FormGroup;
  error: string | null = null;
  saving = false;

  constructor(
    private fb: FormBuilder,
    private communityService: CommunityService,
    private router: Router
  ) {
    this.form = this.fb.group({
      name: [null, Validators.required],
      description: [null, Validators.required]
    });
  }

  submit(): void {
    this.error = null;
    this.saving = true;

    const community: any = {
      name: this.form.value.name,
      description: this.form.value.description,
      creationDate: new Date().toISOString().slice(0, 19),
      suspended: false
    };

    this.communityService.create(community).subscribe({
      next: (created) => {
        this.router.navigate(['/communities', created.id]);
      },
      error: (response: HttpErrorResponse) => {
        // Backend vraca 406 kada zajednica sa istim imenom vec postoji
        this.error = response.status === 406
          ? 'A community with that name already exists.'
          : this.textOf(response);
        this.saving = false;
      }
    });
  }

  // Server salje ili obican tekst ili mapu poruka po polju (npr. kad je ime prazno)
  private textOf(response: HttpErrorResponse): string {
    if (typeof response.error === 'string' && response.error.length > 0) {
      return response.error;
    }
    if (response.error && typeof response.error === 'object') {
      const messages = Object.values(response.error).filter((m) => typeof m === 'string');
      if (messages.length > 0) {
        return messages.join(' ');
      }
    }
    return 'The community could not be created, please check the form.';
  }
}

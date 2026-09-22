import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { EventService } from '../services/event.service';
import { ImageService } from '../services/image.service';
import { CommunityService } from '../../community/services/community.service';
import { Event } from '../model/event.model';
import { Image } from '../model/image.model';
import { Community } from '../../community/model/community.model';
import { EVENT_CATEGORIES } from '../model/event-category.model';

@Component({
  selector: 'app-add-event',
  templateUrl: './add-event.component.html',
  styleUrls: ['./add-event.component.css'],
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false
})
export class AddEventComponent implements OnInit {

  form: FormGroup;
  communities: Community[] = [];
  categories = EVENT_CATEGORIES;
  error: string | null = null;
  uploadedImage: Image | null = null;
  uploading = false;
  saving = false;
  // Isto pravilo kao na backend-u: dogadjaj ne sme da pocne u proslosti
  minDateTime = AddEventComponent.toLocalDateTimeString(new Date());

  private static toLocalDateTimeString(date: Date): string {
    const pad = (n: number) => n.toString().padStart(2, '0');
    return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate())
      + 'T' + pad(date.getHours()) + ':' + pad(date.getMinutes());
  }

  constructor(
    private fb: FormBuilder,
    private eventService: EventService,
    private imageService: ImageService,
    private communityService: CommunityService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.form = this.fb.group({
      title: [null, Validators.required],
      description: [null, Validators.required],
      location: [null, Validators.required],
      startsAt: [null, Validators.required],
      endsAt: [null, Validators.required],
      capacity: [10, [Validators.required, Validators.min(1)]],
      category: ['OTHER', Validators.required],
      belongsToCommunityId: [null]
    });
  }

  ngOnInit(): void {
    // Sa stranice zajednice se dolazi sa njenim id-em, pa je unapred izabrana
    const fromCommunity = Number(this.route.snapshot.queryParamMap.get('community')) || null;

    this.communityService.getMineOrganizing().subscribe({
      next: (result) => {
        this.communities = result;
        if (fromCommunity)
          this.preselectCommunity(fromCommunity);
      },
      error: () => this.communities = []
    });
  }

  // Administrator sme da otvori dogadjaj i u zajednici koju ne vodi, a nje nema u listi, pa se dopisuje
  private preselectCommunity(id: number): void {
    if (this.communities.some((c) => c.id === id)) {
      this.form.patchValue({ belongsToCommunityId: id });
      return;
    }
    this.communityService.getOne(id).subscribe({
      next: (community) => {
        this.communities = [community, ...this.communities];
        this.form.patchValue({ belongsToCommunityId: id });
      },
      error: () => this.form.patchValue({ belongsToCommunityId: null })
    });
  }

  onFileSelected(event: Event | any): void {
    const file: File = event.target.files[0];
    if (!file) {
      return;
    }
    this.uploading = true;
    this.error = null;

    this.imageService.upload(file).subscribe({
      next: (result) => {
        this.uploadedImage = result;
        this.uploading = false;
      },
      error: (response: HttpErrorResponse) => {
        this.error = response.error || 'The image could not be uploaded.';
        this.uploading = false;
      }
    });
  }

  submit(): void {
    this.error = null;
    this.saving = true;

    const event: any = {
      title: this.form.value.title,
      description: this.form.value.description,
      location: this.form.value.location,
      // datetime-local daje 'yyyy-MM-ddTHH:mm', backend ocekuje isti oblik
      startsAt: this.form.value.startsAt,
      endsAt: this.form.value.endsAt,
      capacity: this.form.value.capacity,
      category: this.form.value.category,
      creationDate: new Date().toISOString().slice(0, 19),
      belongsToCommunityId: this.form.value.belongsToCommunityId || null,
      images: this.uploadedImage ? [{ path: this.uploadedImage.path }] : []
    };

    this.eventService.create(event).subscribe({
      next: (created) => {
        this.router.navigate(['/events', created.id]);
      },
      error: (response: HttpErrorResponse) => {
        this.error = this.textOf(response);
        this.saving = false;
      }
    });
  }

  // Server salje ili obican tekst ili mapu poruka po polju (npr. kad je naslov prazan)
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
    return 'The event could not be created, please check the form.';
  }
}

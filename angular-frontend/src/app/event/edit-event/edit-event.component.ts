import { Component, OnInit, ChangeDetectionStrategy } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { EventService } from '../services/event.service';
import { ImageService } from '../services/image.service';
import { Image } from '../model/image.model';
import { EVENT_CATEGORIES } from '../model/event-category.model';

@Component({
  selector: 'app-edit-event',
  templateUrl: './edit-event.component.html',
  styleUrls: ['./edit-event.component.css'],
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false
})
export class EditEventComponent implements OnInit {

  form: FormGroup;
  eventId = 0;
  categories = EVENT_CATEGORIES;
  error: string | null = null;
  currentImage: string | null = null;
  uploadedImage: Image | null = null;
  uploading = false;
  saving = false;
  // Kada je slika sklonjena, uz izmenu se salje prazan spisak pa je backend brise
  imageRemoved = false;
  // Termin dogadjaja koji je poceo se ne prepravlja unazad
  alreadyStarted = false;
  loading = true;
  // Isto pravilo kao na backend-u: dogadjaj ne sme da bude pomeren u proslost
  minDateTime = EditEventComponent.toLocalDateTimeString(new Date());

  private static toLocalDateTimeString(date: Date): string {
    const pad = (n: number) => n.toString().padStart(2, '0');
    return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate())
      + 'T' + pad(date.getHours()) + ':' + pad(date.getMinutes());
  }

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private eventService: EventService,
    private imageService: ImageService,
    private router: Router
  ) {
    this.form = this.fb.group({
      title: [null, Validators.required],
      description: [null, Validators.required],
      location: [null, Validators.required],
      startsAt: [null, Validators.required],
      endsAt: [null, Validators.required],
      capacity: [null, [Validators.required, Validators.min(1)]],
      category: [null, Validators.required]
    });
  }

  ngOnInit(): void {
    this.eventId = Number(this.route.snapshot.paramMap.get('id'));

    this.eventService.getOne(this.eventId).subscribe({
      next: (event) => {
        this.form.patchValue({
          title: event.title,
          description: event.description,
          location: event.location,
          // input type=datetime-local prima najvise do minuta
          startsAt: event.startsAt.slice(0, 16),
          endsAt: event.endsAt.slice(0, 16),
          capacity: event.capacity,
          category: event.category
        });
        if (event.images && event.images.length > 0) {
          this.currentImage = event.images[0].path;
        }
        this.alreadyStarted = new Date(event.startsAt) < new Date();
        if (this.alreadyStarted) {
          this.form.controls['startsAt'].disable();
          this.form.controls['endsAt'].disable();
        }
        this.loading = false;
      },
      error: () => {
        this.error = 'This event could not be loaded.';
        this.loading = false;
      }
    });
  }

  onFileSelected(event: any): void {
    const file: File = event.target.files[0];
    if (!file) {
      return;
    }
    this.uploading = true;
    this.error = null;

    this.imageService.upload(file).subscribe({
      next: (result) => {
        this.uploadedImage = result;
        this.imageRemoved = false;
        this.uploading = false;
      },
      error: (response: HttpErrorResponse) => {
        this.error = response.error || 'The image could not be uploaded.';
        this.uploading = false;
      }
    });
  }

  removeImage(): void {
    this.currentImage = null;
    this.uploadedImage = null;
    this.imageRemoved = true;
  }

  submit(): void {
    this.error = null;
    this.saving = true;

    const changes: any = {
      title: this.form.value.title,
      description: this.form.value.description,
      location: this.form.value.location,
      capacity: this.form.value.capacity,
      category: this.form.value.category
    };

    // Za dogadjaj koji je poceo se termin uopste ne salje
    if (!this.alreadyStarted) {
      changes.startsAt = this.form.value.startsAt;
      changes.endsAt = this.form.value.endsAt;
    }

    // Spisak slika se salje samo ako je izabrana nova ili je stara sklonjena,
    // inace slika ostaje kakva jeste
    if (this.uploadedImage) {
      changes.images = [{ path: this.uploadedImage.path }];
    } else if (this.imageRemoved) {
      changes.images = [];
    }

    this.eventService.update(this.eventId, changes).subscribe({
      next: () => this.router.navigate(['/events', this.eventId]),
      error: (response: HttpErrorResponse) => {
        this.error = typeof response.error === 'string'
          ? response.error
          : 'The event could not be saved.';
        this.saving = false;
      }
    });
  }
}

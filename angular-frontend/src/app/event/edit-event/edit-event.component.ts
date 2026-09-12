import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { EventService } from '../services/event.service';
import { ImageService } from '../services/image.service';
import { Image } from '../model/image.model';

@Component({
  selector: 'app-edit-event',
  templateUrl: './edit-event.component.html',
  styleUrls: ['./edit-event.component.css']
})
export class EditEventComponent implements OnInit {

  form: FormGroup;
  eventId = 0;
  error: string | null = null;
  currentImage: string | null = null;
  uploadedImage: Image | null = null;
  uploading = false;
  saving = false;
  // Kada je slika sklonjena, uz izmenu se salje prazan spisak pa je backend brise
  imageRemoved = false;
  loading = true;

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
      capacity: [null, [Validators.required, Validators.min(1)]]
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
          capacity: event.capacity
        });
        if (event.images && event.images.length > 0) {
          this.currentImage = event.images[0].path;
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
      startsAt: this.form.value.startsAt,
      endsAt: this.form.value.endsAt,
      capacity: this.form.value.capacity
    };

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

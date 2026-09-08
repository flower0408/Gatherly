import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { EventService } from '../services/event.service';
import { ImageService } from '../services/image.service';
import { CommunityService } from '../../community/services/community.service';
import { Event } from '../model/event.model';
import { Image } from '../model/image.model';
import { Community } from '../../community/model/community.model';

@Component({
  selector: 'app-add-event',
  templateUrl: './add-event.component.html',
  styleUrls: ['./add-event.component.css']
})
export class AddEventComponent implements OnInit {

  form: FormGroup;
  communities: Community[] = [];
  error: string | null = null;
  uploadedImage: Image | null = null;
  uploading = false;
  saving = false;

  constructor(
    private fb: FormBuilder,
    private eventService: EventService,
    private imageService: ImageService,
    private communityService: CommunityService,
    private router: Router
  ) {
    this.form = this.fb.group({
      title: [null, Validators.required],
      description: [null, Validators.required],
      location: [null, Validators.required],
      startsAt: [null, Validators.required],
      endsAt: [null, Validators.required],
      capacity: [10, [Validators.required, Validators.min(1)]],
      belongsToCommunityId: [null]
    });
  }

  ngOnInit(): void {
    this.communityService.getMineOrganizing().subscribe({
      next: (result) => this.communities = result,
      error: () => this.communities = []
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
      creationDate: new Date().toISOString().slice(0, 19),
      belongsToCommunityId: this.form.value.belongsToCommunityId || null,
      images: this.uploadedImage ? [{ path: this.uploadedImage.path }] : []
    };

    this.eventService.create(event).subscribe({
      next: (created) => {
        this.router.navigate(['/events', created.id]);
      },
      error: (response: HttpErrorResponse) => {
        this.error = typeof response.error === 'string'
          ? response.error
          : 'The event could not be created, please check the form.';
        this.saving = false;
      }
    });
  }
}

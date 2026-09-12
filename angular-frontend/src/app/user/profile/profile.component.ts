import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { UserService } from '../services/user.service';
import { AuthenticationService } from '../services/authentication.service';
import { ImageService } from '../../event/services/image.service';
import { BanService } from '../../banned/services/ban.service';
import { CommunityService } from '../../community/services/community.service';
import { EventService } from '../../event/services/event.service';
import { User } from '../model/user.model';
import { Community } from '../../community/model/community.model';
import { Event } from '../../event/model/event.model';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {

  user: User | null = null;
  loading = true;
  notFound = false;
  // Bez id-a u ruti prikazuje se profil prijavljenog korisnika
  isMine = false;
  message: string | null = null;
  error: string | null = null;

  editing = false;
  firstName = '';
  lastName = '';
  displayName = '';
  description = '';
  saving = false;

  changingPassword = false;
  oldPassword = '';
  newPassword = '';
  repeatPassword = '';

  uploading = false;

  // Sta taj korisnik radi u aplikaciji, zbog cega se profil uopste otvara
  communities: Community[] = [];
  organizing: Event[] = [];

  constructor(
    private route: ActivatedRoute,
    private userService: UserService,
    private imageService: ImageService,
    private banService: BanService,
    private communityService: CommunityService,
    private eventService: EventService,
    public auth: AuthenticationService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');

    const request = id ? this.userService.getOne(Number(id)) : this.userService.whoAmI();

    request.subscribe({
      next: (result) => {
        this.user = result;
        this.loading = false;
        this.checkOwnership(result);
        this.loadActivity(result.id);
      },
      error: () => {
        this.notFound = true;
        this.loading = false;
      }
    });
  }

  private loadActivity(userId: number): void {
    this.communityService.getForUser(userId).subscribe({
      next: (result) => this.communities = result,
      error: () => this.communities = []
    });

    this.eventService.getForUser(userId).subscribe({
      next: (result) => this.organizing = result,
      error: () => this.organizing = []
    });
  }

  // Skor se pokazuje tek kada ima pokrice, i to samo prijavljenima.
  // Jedan zabelezen izostanak ne sme da obelezi coveka.
  get showsReliability(): boolean {
    return this.auth.isLoggedIn()
      && this.user !== null
      && this.user.reliability !== null
      && this.user.attendanceCount >= 3;
  }

  get reliabilityText(): string {
    if (!this.user || this.user.reliability === null) {
      return '';
    }
    const attended = Math.round((this.user.reliability / 100) * this.user.attendanceCount);
    return 'Turned up to ' + attended + ' of the ' + this.user.attendanceCount
      + ' events they had a place at.';
  }

  get reliabilityClass(): string {
    const score = this.user?.reliability;
    if (score === null || score === undefined) {
      return 'text-body-secondary';
    }
    if (score >= 80) {
      return 'text-success';
    }
    return score >= 50 ? 'text-warning' : 'text-danger';
  }

  private checkOwnership(user: User): void {
    this.userService.whoAmI().subscribe({
      next: (me) => this.isMine = me.id === user.id,
      error: () => this.isMine = false
    });
  }

  startEdit(): void {
    this.editing = true;
    this.changingPassword = false;
    this.clearNotices();
    this.firstName = this.user?.firstName || '';
    this.lastName = this.user?.lastName || '';
    this.displayName = this.user?.displayName || '';
    this.description = this.user?.description || '';
  }

  startPasswordChange(): void {
    this.changingPassword = true;
    this.editing = false;
    this.clearNotices();
    this.oldPassword = '';
    this.newPassword = '';
    this.repeatPassword = '';
  }

  cancel(): void {
    this.editing = false;
    this.changingPassword = false;
    this.clearNotices();
  }

  saveProfile(): void {
    if (!this.user) {
      return;
    }
    this.clearNotices();
    this.saving = true;

    this.userService.update({
      id: this.user.id,
      firstName: this.firstName,
      lastName: this.lastName,
      displayName: this.displayName,
      description: this.description
    }).subscribe({
      next: (updated) => {
        this.user = updated;
        this.editing = false;
        this.saving = false;
        this.message = 'Your profile has been saved.';
      },
      error: (response: HttpErrorResponse) => {
        this.error = this.textOf(response);
        this.saving = false;
      }
    });
  }

  savePassword(): void {
    this.clearNotices();
    this.saving = true;

    this.userService.changePassword(this.oldPassword, this.newPassword).subscribe({
      next: () => {
        this.changingPassword = false;
        this.saving = false;
        this.message = 'Your password has been changed. We sent you a message about it.';
      },
      error: (response: HttpErrorResponse) => {
        this.error = this.textOf(response);
        this.saving = false;
      }
    });
  }

  onFileSelected(event: any): void {
    const input = event.target as HTMLInputElement;
    const file: File = input.files ? input.files[0] : null as any;
    if (!file) {
      return;
    }
    this.clearNotices();
    this.uploading = true;

    // Slika se prvo otprema, pa se tek onda vezuje za nalog
    this.imageService.upload(file).subscribe({
      next: (image) => {
        this.userService.setProfileImage(image.path).subscribe({
          next: (saved) => {
            // Otpremanje vraca samo putanju, a zapis o profilnoj slici tek ovaj poziv
            if (this.user) {
              this.user.profileImage = saved;
            }
            this.uploading = false;
            this.message = 'Your picture has been updated.';
            input.value = '';
          },
          error: (response: HttpErrorResponse) => {
            this.error = this.textOf(response);
            this.uploading = false;
            input.value = '';
          }
        });
      },
      error: (response: HttpErrorResponse) => {
        this.error = this.textOf(response);
        this.uploading = false;
        input.value = '';
      }
    });
  }

  // Uklanjanje slike vraca profil na pocetno slovo u krugu
  removePicture(): void {
    if (!this.user || !this.user.profileImage || !confirm('Remove your picture?')) {
      return;
    }
    this.clearNotices();

    this.imageService.delete(this.user.profileImage.id).subscribe({
      next: () => {
        if (this.user) {
          this.user.profileImage = null;
        }
        this.clearFileInput();
        this.message = 'Your picture has been removed.';
      },
      error: (response: HttpErrorResponse) => this.error = this.textOf(response)
    });
  }

  banFromSystem(): void {
    if (!this.user || !confirm('Ban ' + this.user.username + ' from Gatherly? They will not be able to sign in.')) {
      return;
    }
    this.clearNotices();

    this.banService.banFromSystem(this.user.id).subscribe({
      next: () => this.message = this.user!.username + ' has been banned from Gatherly.',
      error: (response: HttpErrorResponse) => this.error = this.textOf(response)
    });
  }

  // Polje za fajl pamti ime i posle uklanjanja slike, pa se prazni rucno
  private clearFileInput(): void {
    const input = document.getElementById('picture') as HTMLInputElement;
    if (input) {
      input.value = '';
    }
  }

  private clearNotices(): void {
    this.message = null;
    this.error = null;
  }

  // Server salje ili obican tekst ili mapu poruka po polju, pa se citaju oba oblika
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
    return 'That action could not be completed.';
  }
}

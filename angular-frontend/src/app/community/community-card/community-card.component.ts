import { Component, Input, ChangeDetectionStrategy } from '@angular/core';
import { Community } from '../model/community.model';

@Component({
  selector: 'app-community-card',
  templateUrl: './community-card.component.html',
  styleUrls: ['./community-card.component.css'],
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false
})
export class CommunityCardComponent {

  @Input() community!: Community;
}

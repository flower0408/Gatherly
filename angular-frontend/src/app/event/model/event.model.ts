import { Image } from './image.model';

export class Event {
  id: number = 0;
  title: string = '';
  description: string = '';
  location: string = '';
  startsAt: string = '';
  endsAt: string = '';
  capacity: number = 0;
  creationDate: string = '';
  createdByUserId: number | null = null;
  belongsToCommunityId: number | null = null;
  takenSpots: number = 0;
  images: Image[] = [];
}

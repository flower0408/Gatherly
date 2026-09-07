import { Image } from '../../event/model/image.model';

export class User {
  id: number = 0;
  username: string = '';
  email: string = '';
  role: string = '';
  lastLogin: string | null = null;
  firstName: string = '';
  lastName: string = '';
  displayName: string | null = null;
  description: string | null = null;
  verified: boolean = false;
  profileImage: Image | null = null;
}

export class Ban {
  id: number = 0;
  timestamp: string = '';
  bannedByUserId: number | null = null;
  towardsUserId: number | null = null;
  // Prazno znaci blokada na nivou celog sistema
  forCommunityId: number | null = null;
  towardsUsername: string | null = null;
  // Naziv zajednice, prazno kod blokade na nivou sistema
  communityName: string | null = null;
}
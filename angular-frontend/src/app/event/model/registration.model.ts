export class Registration {
  id: number = 0;
  status: string = '';
  createdAt: string = '';
  at: string | null = null;
  createdByUserId: number = 0;
  forEventId: number = 0;
  participantUsername: string | null = null;
  participantReliability: number | null = null;
  // Upozorenje da se dogadjaj preklapa sa necim na sta je korisnik vec prijavljen
  conflictsWith: string | null = null;
}

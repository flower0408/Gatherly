export class Report {
  id: number = 0;
  reason: string = '';
  timestamp: string = '';
  byUserId: number = 0;
  // Dok je null, prijava jos nije pregledana
  accepted: boolean | null = null;
  onUserId: number | null = null;
  onEventId: number | null = null;
  onCommentId: number | null = null;
  // Kratak opis prijavljene stvari i dogadjaj na kome se nalazi, stizu sa servera
  targetLabel: string | null = null;
  targetEventId: number | null = null;
}
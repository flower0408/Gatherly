export class Comment {
  id: number = 0;
  text: string = '';
  timestamp: string = '';
  repliesToCommentId: number | null = null;
  belongsToUserId: number = 0;
  belongsToEventId: number | null = null;
  // Ime autora i broj reakcija stizu sa servera, radi prikaza uz komentar
  authorUsername: string | null = null;
  reactions: { [type: string]: number } | null = null;
}
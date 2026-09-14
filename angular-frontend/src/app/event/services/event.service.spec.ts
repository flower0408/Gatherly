import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { EventService } from './event.service';
import { Event } from '../model/event.model';

// Servis se proverava bez pravog servera: HttpClientTestingModule presrece zahtev,
// pa se moze videti tacno koja je adresa pozvana i sa kojim parametrima.
describe('EventService', () => {

  let service: EventService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [EventService]
    });

    service = TestBed.inject(EventService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  // Posle svakog testa se proverava da nije ostao nijedan neocekivan zahtev
  afterEach(() => {
    httpMock.verify();
  });

  it('asks for upcoming events', () => {
    service.getUpcoming().subscribe();

    const request = httpMock.expectOne('api/events');
    expect(request.request.method).toBe('GET');
    request.flush([]);
  });

  it('sends only the filters that were filled in', () => {
    service.search('run', '', '', '', false).subscribe();

    const request = httpMock.expectOne((r) => r.url === 'api/events/search');
    expect(request.request.params.get('term')).toBe('run');
    expect(request.request.params.has('category')).toBeFalse();
    expect(request.request.params.has('from')).toBeFalse();
    expect(request.request.params.has('upcoming')).toBeFalse();
    request.flush([]);
  });

  it('sends every filter when all of them are given', () => {
    service.search('run', 'SPORT', '2026-09-01', '2026-09-30', true).subscribe();

    const request = httpMock.expectOne((r) => r.url === 'api/events/search');
    expect(request.request.params.get('term')).toBe('run');
    expect(request.request.params.get('category')).toBe('SPORT');
    expect(request.request.params.get('from')).toBe('2026-09-01');
    expect(request.request.params.get('to')).toBe('2026-09-30');
    expect(request.request.params.get('upcoming')).toBe('true');
    request.flush([]);
  });

  it('returns the events the server sent', () => {
    const sent: Event[] = [Object.assign(new Event(), { id: 1, title: 'Catan tournament' })];
    let received: Event[] = [];

    service.getUpcoming().subscribe((result) => received = result);
    httpMock.expectOne('api/events').flush(sent);

    expect(received.length).toBe(1);
    expect(received[0].title).toBe('Catan tournament');
  });

  it('sends a new event with the post method', () => {
    const event = Object.assign(new Event(), { title: 'Jazz in the park' });

    service.create(event).subscribe();

    const request = httpMock.expectOne('api/events/add');
    expect(request.request.method).toBe('POST');
    expect(request.request.body.title).toBe('Jazz in the park');
    request.flush(event);
  });
});

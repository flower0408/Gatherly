import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { RouterTestingModule } from '@angular/router/testing';
import { MyRegistrationsComponent } from './my-registrations.component';
import { Registration } from '../model/registration.model';
import { Event } from '../model/event.model';
import { provideHttpClient, withInterceptorsFromDi, withXhr } from '@angular/common/http';

// Komponenta prevodi stanja prijave u recenice koje korisnik razume i odlucuje
// kada se nudi otkazivanje. Oba pravila se proveravaju bez pravog servera.
describe('MyRegistrationsComponent', () => {

  let component: MyRegistrationsComponent;
  let fixture: ComponentFixture<MyRegistrationsComponent>;

  function registrationFor(status: string, eventId: number): Registration {
    return Object.assign(new Registration(), { id: 1, status: status, forEventId: eventId });
  }

  function eventStartingAt(id: number, startsAt: string): Event {
    return Object.assign(new Event(), { id: id, title: 'Catan tournament', startsAt: startsAt });
  }

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [MyRegistrationsComponent],
      imports: [RouterTestingModule],
      providers: [
        provideHttpClient(withXhr(), withInterceptorsFromDi()),
        provideHttpClientTesting()
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(MyRegistrationsComponent);
    component = fixture.componentInstance;
  });

  it('is created', () => {
    expect(component).toBeTruthy();
  });

  it('describes every status in words', () => {
    expect(component.label('PENDING')).toBe('Waiting for the organizer');
    expect(component.label('WAITLISTED')).toBe('On the waiting list');
    expect(component.label('ACCEPTED')).toBe('Going');
    expect(component.label('REJECTED')).toBe('Not accepted');
    expect(component.label('CANCELLED')).toBe('Cancelled');
    expect(component.label('ATTENDED')).toBe('Attended');
    expect(component.label('NO_SHOW')).toBe('No-show');
  });

  it('marks a confirmed place green and a missed one red', () => {
    expect(component.badge('ACCEPTED')).toContain('success');
    expect(component.badge('ATTENDED')).toContain('success');
    expect(component.badge('NO_SHOW')).toContain('danger');
    expect(component.badge('REJECTED')).toContain('danger');
    expect(component.badge('WAITLISTED')).toContain('warning');
  });

  it('offers cancelling while the event is still ahead', () => {
    const event = eventStartingAt(9, '2099-01-01T18:00:00');
    component.events[9] = event;

    expect(component.canCancel(registrationFor('ACCEPTED', 9))).toBeTrue();
    expect(component.canCancel(registrationFor('PENDING', 9))).toBeTrue();
    expect(component.canCancel(registrationFor('WAITLISTED', 9))).toBeTrue();
  });

  // Sto je proslo, ne otkazuje se
  it('does not offer cancelling once the event has happened', () => {
    component.events[9] = eventStartingAt(9, '2020-01-01T18:00:00');

    expect(component.canCancel(registrationFor('ACCEPTED', 9))).toBeFalse();
  });

  it('does not offer cancelling for a registration that is already settled', () => {
    component.events[9] = eventStartingAt(9, '2099-01-01T18:00:00');

    expect(component.canCancel(registrationFor('CANCELLED', 9))).toBeFalse();
    expect(component.canCancel(registrationFor('REJECTED', 9))).toBeFalse();
  });

  it('does not offer cancelling before the event is known', () => {
    expect(component.canCancel(registrationFor('ACCEPTED', 404))).toBeFalse();
  });
});

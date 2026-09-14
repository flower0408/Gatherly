package rs.ac.uns.ftn.eventhub.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rs.ac.uns.ftn.eventhub.model.entity.Event;
import rs.ac.uns.ftn.eventhub.model.entity.EventRegistration;
import rs.ac.uns.ftn.eventhub.model.entity.User;
import rs.ac.uns.ftn.eventhub.model.enums.RegistrationStatus;
import rs.ac.uns.ftn.eventhub.repository.EventRegistrationRepository;
import rs.ac.uns.ftn.eventhub.service.implementation.EventRegistrationServiceImpl;
import rs.ac.uns.ftn.eventhub.service.implementation.UserServiceImpl;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Provera dva pravila koja su srz ove teme: skor pouzdanosti ucesnika
// i automatsko promovisanje prvog sa liste cekanja kada se oslobodi mesto.
@ExtendWith(MockitoExtension.class)
class EventRegistrationServiceTest {

    @Mock
    private EventRegistrationRepository registrationRepository;

    @Mock
    private UserServiceImpl userService;

    @Mock
    private MailService mailService;

    @InjectMocks
    private EventRegistrationServiceImpl registrationService;

    private Event event;

    private User participant;

    @BeforeEach
    void setUp() {
        participant = new User();
        participant.setId(1L);

        event = new Event();
        event.setId(10L);
        event.setCapacity(2);
    }

    // Skor je procenat dolazaka u odnosu na dogadjaje na kojima se ucesnik ocekivao
    @Test
    void reliabilityIsTheShareOfEventsTheParticipantTurnedUpTo() {
        when(registrationRepository.countByUserAndStatus(1L, RegistrationStatus.ATTENDED.name())).thenReturn(3);
        when(registrationRepository.countByUserAndStatus(1L, RegistrationStatus.NO_SHOW.name())).thenReturn(1);

        assertEquals(75, registrationService.calculateReliability(1L));
    }

    @Test
    void reliabilityIsAHundredWhenTheParticipantAlwaysTurnedUp() {
        when(registrationRepository.countByUserAndStatus(1L, RegistrationStatus.ATTENDED.name())).thenReturn(4);
        when(registrationRepository.countByUserAndStatus(1L, RegistrationStatus.NO_SHOW.name())).thenReturn(0);

        assertEquals(100, registrationService.calculateReliability(1L));
    }

    // Bez ijednog zabelezenog ishoda skor ne postoji, da se ne bi prikazivala nula bez pokrica
    @Test
    void reliabilityIsUnknownWithoutAnyHistory() {
        when(registrationRepository.countByUserAndStatus(1L, RegistrationStatus.ATTENDED.name())).thenReturn(0);
        when(registrationRepository.countByUserAndStatus(1L, RegistrationStatus.NO_SHOW.name())).thenReturn(0);

        assertNull(registrationService.calculateReliability(1L));
    }

    @Test
    void attendanceCountAddsUpBothOutcomes() {
        when(registrationRepository.countByUserAndStatus(1L, RegistrationStatus.ATTENDED.name())).thenReturn(2);
        when(registrationRepository.countByUserAndStatus(1L, RegistrationStatus.NO_SHOW.name())).thenReturn(3);

        assertEquals(5, registrationService.countAttendanceRecords(1L));
    }

    // Kada se mesto oslobodi, prvi sa liste cekanja dobija potvrdjeno mesto i obavestenje
    @Test
    void freedSpotPromotesTheFirstPersonOnTheWaitingList() {
        EventRegistration waiting = new EventRegistration();
        waiting.setId(5L);
        waiting.setStatus(RegistrationStatus.WAITLISTED);
        waiting.setCreatedBy(participant);

        when(registrationRepository.countTakenSpots(10L)).thenReturn(1);
        when(registrationRepository.findFirstWaitlisted(10L)).thenReturn(Optional.of(waiting));
        when(registrationRepository.save(any(EventRegistration.class))).thenReturn(waiting);
        when(userService.findById(1L)).thenReturn(participant);

        registrationService.promoteFromWaitlist(event);

        assertEquals(RegistrationStatus.ACCEPTED, waiting.getStatus());
        verify(mailService, times(1)).sendPromotedFromWaitlistMail(participant, event);
    }

    // Dokle god je dogadjaj pun, niko se ne promovise
    @Test
    void nobodyIsPromotedWhileTheEventIsStillFull() {
        when(registrationRepository.countTakenSpots(10L)).thenReturn(2);

        registrationService.promoteFromWaitlist(event);

        verify(registrationRepository, never()).findFirstWaitlisted(any());
        verify(mailService, never()).sendPromotedFromWaitlistMail(any(), any());
    }

    @Test
    void nothingHappensWhenTheWaitingListIsEmpty() {
        when(registrationRepository.countTakenSpots(10L)).thenReturn(0);
        when(registrationRepository.findFirstWaitlisted(10L)).thenReturn(Optional.empty());

        registrationService.promoteFromWaitlist(event);

        verify(registrationRepository, never()).save(any(EventRegistration.class));
        verify(mailService, never()).sendPromotedFromWaitlistMail(any(), any());
    }

    @Test
    void promotingIgnoresAMissingEvent() {
        registrationService.promoteFromWaitlist(null);

        verify(mailService, never()).sendPromotedFromWaitlistMail(any(), any());
    }
}

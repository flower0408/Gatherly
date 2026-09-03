package rs.ac.uns.ftn.eventhub.service;

import rs.ac.uns.ftn.eventhub.model.entity.Event;
import rs.ac.uns.ftn.eventhub.model.entity.EventRegistration;
import rs.ac.uns.ftn.eventhub.model.entity.User;
import rs.ac.uns.ftn.eventhub.model.enums.RegistrationStatus;

import java.util.List;

public interface EventRegistrationService {

    EventRegistration findById(Long id);

    List<EventRegistration> findRegistrationsForEvent(Long eventId);

    List<EventRegistration> findRegistrationsForUser(Long userId);

    EventRegistration findActiveRegistration(Long userId, Long eventId);

    Integer countTakenSpots(Long eventId);

    EventRegistration findFirstWaitlisted(Long eventId);

    EventRegistration createRegistration(User user, Event event, RegistrationStatus status);

    EventRegistration updateStatus(EventRegistration registration, RegistrationStatus status);

    EventRegistration saveRegistration(EventRegistration registration);

    Integer deleteRegistrationsForEvent(Long eventId);
}

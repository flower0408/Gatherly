package rs.ac.uns.ftn.eventhub.service;

import rs.ac.uns.ftn.eventhub.model.dto.EventDTO;
import rs.ac.uns.ftn.eventhub.model.entity.Event;
import rs.ac.uns.ftn.eventhub.model.entity.User;

import java.util.List;

public interface EventService {

    Event findById(Long id);

    List<Event> findAll();

    List<Event> findUpcoming();

    List<Event> findEventsForCreator(Long userId);

    List<Event> findEventsForCommunity(Long communityId);

    List<Event> findEventsForCommunityAsc(Long communityId);

    List<Event> findEventsForCommunityDesc(Long communityId);

    List<Event> findHomepageEvents(Long userId);

    Long findCommunityIdForEvent(Long eventId);

    String findConflictingEventTitle(Long userId, Event event);

    Event createEvent(EventDTO eventDTO, User createdBy);

    Event updateEvent(Event event);

    Boolean addEventToCommunity(Long communityId, Long eventId);

    Integer deleteEventFromCommunity(Long eventId);

    Integer deleteEvent(Long id);
}

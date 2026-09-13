package rs.ac.uns.ftn.eventhub.service.implementation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.eventhub.model.dto.EventDTO;
import rs.ac.uns.ftn.eventhub.model.entity.Community;
import rs.ac.uns.ftn.eventhub.model.entity.Event;
import rs.ac.uns.ftn.eventhub.model.entity.User;
import rs.ac.uns.ftn.eventhub.model.enums.EventCategory;
import rs.ac.uns.ftn.eventhub.repository.EventRepository;
import rs.ac.uns.ftn.eventhub.service.CommentService;
import rs.ac.uns.ftn.eventhub.service.EventRegistrationService;
import rs.ac.uns.ftn.eventhub.service.EventService;
import rs.ac.uns.ftn.eventhub.service.ImageService;
import rs.ac.uns.ftn.eventhub.service.ReactionService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class EventServiceImpl implements EventService {


    private EventRepository eventRepository;


    private EventRegistrationService registrationService;


    private CommentService commentService;


    private ReactionService reactionService;


    private ImageService imageService;


    @Autowired
    public EventServiceImpl(EventRepository eventRepository, EventRegistrationService registrationService,
                            CommentService commentService, ReactionService reactionService,
                            ImageService imageService) {
        this.eventRepository = eventRepository;
        this.registrationService = registrationService;
        this.commentService = commentService;
        this.reactionService = reactionService;
        this.imageService = imageService;
    }

    // Brisanje dogadjaja sa svim sto uz njega ide. Stoji na jednom mestu, jer dogadjaj
    // ne brise samo njegov tvorac nego i brisanje zajednice kojoj pripada.
    @Override
    public void deleteEventWithContent(Long id) {
        logger.info("Deleting event with id: " + id + " and everything that belongs to it");
        reactionService.deleteReactionsForEvent(id);
        commentService.deleteCommentsForEvent(id);
        imageService.deleteImagesForEvent(id);
        registrationService.deleteRegistrationsForEvent(id);
        deleteEventFromCommunity(id);
        deleteEvent(id);
    }

    private static final Logger logger = LogManager.getLogger(EventServiceImpl.class);

    @Override
    public Event findById(Long id) {
        Optional<Event> event = eventRepository.findById(id);
        if (!event.isEmpty())
            return event.get();
        logger.error("Repository search for event with id: " + id + " returned null");
        return null;
    }

    @Override
    public List<Event> findAll() {
        return this.eventRepository.findAllActiveEvents().orElse(Collections.emptyList());
    }

    @Override
    public List<Event> findUpcoming() {
        return this.eventRepository.findUpcomingEvents().orElse(Collections.emptyList());
    }

    @Override
    public List<Event> searchEvents(String term, String category, String fromDate, String toDate,
                                    boolean onlyUpcoming) {
        return this.eventRepository.searchEvents(term, category, fromDate, toDate, onlyUpcoming)
                .orElse(Collections.emptyList());
    }

    @Override
    public List<Event> findEventsForCreator(Long userId) {
        return this.eventRepository.findEventsByCreator(userId).orElse(Collections.emptyList());
    }

    @Override
    public List<Event> findEventsForCommunity(Long communityId) {
        return this.eventRepository.findEventsByCommunityId(communityId).orElse(Collections.emptyList());
    }

    @Override
    public List<Event> findEventsForCommunityAsc(Long communityId) {
        return this.eventRepository.findEventsByCommunityIdAsc(communityId).orElse(Collections.emptyList());
    }

    @Override
    public List<Event> findEventsForCommunityDesc(Long communityId) {
        return this.eventRepository.findEventsByCommunityIdDesc(communityId).orElse(Collections.emptyList());
    }

    @Override
    public List<Event> findHomepageEvents(Long userId) {
        return this.eventRepository.findHomepageEvents(userId).orElse(Collections.emptyList());
    }

    @Override
    public Long findCommunityIdForEvent(Long eventId) {
        return eventRepository.findCommunityIdByEventId(eventId).orElse(null);
    }

    @Override
    public String findConflictingEventTitle(Long userId, Event event) {
        return eventRepository.findConflictingEventTitle(userId, event.getId(),
                event.getStartsAt(), event.getEndsAt()).orElse(null);
    }

    @Override
    public Event createEvent(EventDTO eventDTO, User createdBy) {
        Event newEvent = new Event();
        newEvent.setTitle(eventDTO.getTitle());
        newEvent.setDescription(eventDTO.getDescription());
        newEvent.setLocation(eventDTO.getLocation());
        newEvent.setStartsAt(LocalDateTime.parse(eventDTO.getStartsAt()));
        newEvent.setEndsAt(LocalDateTime.parse(eventDTO.getEndsAt()));
        newEvent.setCapacity(eventDTO.getCapacity());
        newEvent.setCategory(EventCategory.valueOf(eventDTO.getCategory().toUpperCase()));
        newEvent.setCreationDate(LocalDateTime.parse(eventDTO.getCreationDate()));
        newEvent.setCreatedBy(createdBy);
        newEvent.setDeleted(false);
        newEvent = eventRepository.save(newEvent);

        return newEvent;
    }

    @Override
    public Event updateEvent(Event event) {
        return eventRepository.save(event);
    }

    @Override
    public Boolean addEventToCommunity(Long communityId, Long eventId) {
        return eventRepository.saveCommunityEvent(communityId, eventId) > 0;
    }

    @Override
    public Integer deleteEventFromCommunity(Long eventId) {
        return eventRepository.deleteEventFromCommunity(eventId);
    }

    @Override
    public Integer deleteEvent(Long id) {
        return eventRepository.deleteEventById(id);
    }
}

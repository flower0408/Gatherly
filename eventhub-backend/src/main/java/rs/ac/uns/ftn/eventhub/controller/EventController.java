package rs.ac.uns.ftn.eventhub.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.eventhub.model.dto.EventDTO;
import rs.ac.uns.ftn.eventhub.model.dto.ImageDTO;
import rs.ac.uns.ftn.eventhub.model.entity.Community;
import rs.ac.uns.ftn.eventhub.model.entity.Image;
import rs.ac.uns.ftn.eventhub.model.entity.Event;
import rs.ac.uns.ftn.eventhub.model.entity.User;
import rs.ac.uns.ftn.eventhub.model.enums.EventCategory;
import rs.ac.uns.ftn.eventhub.security.TokenUtils;
import rs.ac.uns.ftn.eventhub.service.BannedService;
import rs.ac.uns.ftn.eventhub.service.CommentService;
import rs.ac.uns.ftn.eventhub.service.CommunityService;
import rs.ac.uns.ftn.eventhub.service.EventRegistrationService;
import rs.ac.uns.ftn.eventhub.service.EventService;
import rs.ac.uns.ftn.eventhub.service.ImageService;
import rs.ac.uns.ftn.eventhub.service.ReactionService;
import rs.ac.uns.ftn.eventhub.service.UserService;
import rs.ac.uns.ftn.eventhub.service.implementation.BannedServiceImpl;
import rs.ac.uns.ftn.eventhub.service.implementation.CommentServiceImpl;
import rs.ac.uns.ftn.eventhub.service.implementation.CommunityServiceImpl;
import rs.ac.uns.ftn.eventhub.service.implementation.EventRegistrationServiceImpl;
import rs.ac.uns.ftn.eventhub.service.implementation.EventServiceImpl;
import rs.ac.uns.ftn.eventhub.service.implementation.ImageServiceImpl;
import rs.ac.uns.ftn.eventhub.service.implementation.ReactionServiceImpl;
import rs.ac.uns.ftn.eventhub.service.implementation.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("api/events")
public class EventController {


    EventService eventService;


    CommunityService communityService;


    UserService userService;


    EventRegistrationService registrationService;


    ImageService imageService;


    CommentService commentService;


    ReactionService reactionService;


    BannedService bannedService;


    TokenUtils tokenUtils;

    private static final Logger logger = LogManager.getLogger(EventController.class);

    @Autowired
    public EventController(EventServiceImpl eventService, CommunityServiceImpl communityService,
                           UserServiceImpl userService, EventRegistrationServiceImpl registrationService,
                           ImageServiceImpl imageService, CommentServiceImpl commentService,
                           ReactionServiceImpl reactionService, BannedServiceImpl bannedService,
                           TokenUtils tokenUtils) {
        this.eventService = eventService;
        this.communityService = communityService;
        this.userService = userService;
        this.registrationService = registrationService;
        this.imageService = imageService;
        this.commentService = commentService;
        this.reactionService = reactionService;
        this.bannedService = bannedService;
        this.tokenUtils = tokenUtils;
    }

    // Rute za pregled su otvorene i za goste, zato ne traze token

    @GetMapping()
    public ResponseEntity<List<EventDTO>> getAll() {
        logger.info("Finding all upcoming events");
        logger.info("Created and sent response");

        return new ResponseEntity<>(toDTOs(eventService.findUpcoming()), HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<List<EventDTO>> getAllIncludingPast() {
        logger.info("Finding all events, including the ones that already happened");
        logger.info("Created and sent response");

        return new ResponseEntity<>(toDTOs(eventService.findAll()), HttpStatus.OK);
    }

    // Pretraga je otvorena i za goste, jer je pregled dogadjaja javan.
    // Prazan parametar znaci da se taj filter ne primenjuje.
    @GetMapping("/search")
    public ResponseEntity<?> search(@RequestParam(value = "term", required = false) String term,
                                    @RequestParam(value = "category", required = false) String category,
                                    @RequestParam(value = "from", required = false) String from,
                                    @RequestParam(value = "to", required = false) String to,
                                    @RequestParam(value = "upcoming", required = false) Boolean upcoming) {
        if (category != null && !category.isBlank() && parseCategory(category) == null) {
            logger.error("Unknown event category in search: " + category);
            return new ResponseEntity<>("Unknown event category.", HttpStatus.BAD_REQUEST);
        }
        String cleanTerm = (term == null || term.isBlank()) ? null : term.trim();
        String cleanCategory = (category == null || category.isBlank()) ? null : category.toUpperCase();
        // Datum stize kao yyyy-MM-dd, a pretrazuje se pocetak odnosno kraj tog dana
        String fromMoment = (from == null || from.isBlank()) ? null : from + " 00:00:00";
        String toMoment = (to == null || to.isBlank()) ? null : to + " 23:59:59";

        logger.info("Searching events by term: " + cleanTerm + ", category: " + cleanCategory);
        List<Event> found = eventService.searchEvents(cleanTerm, cleanCategory, fromMoment, toMoment,
                Boolean.TRUE.equals(upcoming));
        logger.info("Created and sent response");

        return new ResponseEntity<>(toDTOs(found), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDTO> getOne(@PathVariable String id) {
        logger.info("Finding event with id: " + id);
        Event event = eventService.findById(Long.parseLong(id));
        if (event == null) {
            logger.error("Event not found with id: " + id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        logger.info("Created and sent response");

        return new ResponseEntity<>(toDTO(event), HttpStatus.OK);
    }

    @GetMapping("/community/{communityId}")
    public ResponseEntity<List<EventDTO>> getAllForCommunity(@PathVariable String communityId) {
        logger.info("Finding events of community with id: " + communityId);
        logger.info("Created and sent response");

        return new ResponseEntity<>(toDTOs(eventService.findEventsForCommunity(Long.parseLong(communityId))), HttpStatus.OK);
    }

    @GetMapping("/community/{communityId}/sort/{order}")
    public ResponseEntity<List<EventDTO>> getAllForCommunitySorted(@PathVariable String communityId, @PathVariable String order) {
        logger.info("Finding events of community with id: " + communityId + " sorted " + order);
        List<Event> events;
        if (order.equals("desc"))
            events = eventService.findEventsForCommunityDesc(Long.parseLong(communityId));
        else
            events = eventService.findEventsForCommunityAsc(Long.parseLong(communityId));
        logger.info("Created and sent response");

        return new ResponseEntity<>(toDTOs(events), HttpStatus.OK);
    }

    // Dogadjaji koje jedan korisnik organizuje, za prikaz na njegovom profilu.
    // Prosli se izostavljaju, jer profil govori sta tek predstoji.
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<EventDTO>> getUpcomingEventsOfUser(@PathVariable String userId) {
        logger.info("Finding upcoming events created by user with id: " + userId);
        List<Event> upcoming = new ArrayList<>();
        for (Event temp : eventService.findEventsForCreator(Long.parseLong(userId))) {
            if (temp.getStartsAt().isAfter(LocalDateTime.now()))
                upcoming.add(temp);
        }
        logger.info("Created and sent response");

        return new ResponseEntity<>(toDTOs(upcoming), HttpStatus.OK);
    }

    // Od ove tacke rute traze prijavljenog korisnika

    @GetMapping("/homepage")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<EventDTO>> getHomepageEvents(@RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        logger.info("Finding homepage events for user with id: " + user.getId());
        logger.info("Created and sent response");

        return new ResponseEntity<>(toDTOs(eventService.findHomepageEvents(user.getId())), HttpStatus.OK);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<EventDTO>> getMyEvents(@RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        logger.info("Finding events created by user with id: " + user.getId());
        logger.info("Created and sent response");

        return new ResponseEntity<>(toDTOs(eventService.findEventsForCreator(user.getId())), HttpStatus.OK);
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> addEvent(@RequestBody @Validated EventDTO newEvent, @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (newEvent.getCapacity() == null || newEvent.getCapacity() < 1) {
            logger.error("Event capacity must be at least one");
            return new ResponseEntity<>("Capacity must be at least one.", HttpStatus.BAD_REQUEST);
        }
        if (newEvent.getStartsAt() == null || newEvent.getEndsAt() == null) {
            logger.error("Event must have a start and an end");
            return new ResponseEntity<>("An event must have a start and an end.", HttpStatus.BAD_REQUEST);
        }
        if (LocalDateTime.parse(newEvent.getStartsAt()).isBefore(LocalDateTime.now())) {
            logger.error("Event cannot start in the past");
            return new ResponseEntity<>("An event cannot start in the past.", HttpStatus.BAD_REQUEST);
        }
        if (!LocalDateTime.parse(newEvent.getEndsAt()).isAfter(LocalDateTime.parse(newEvent.getStartsAt()))) {
            logger.error("Event cannot end before it starts");
            return new ResponseEntity<>("An event must end after it starts.", HttpStatus.BAD_REQUEST);
        }
        if (parseCategory(newEvent.getCategory()) == null) {
            logger.error("Unknown event category: " + newEvent.getCategory());
            return new ResponseEntity<>("Unknown event category.", HttpStatus.BAD_REQUEST);
        }
        // Dogadjaj moze da pripada zajednici, ali ne mora
        Community community = null;
        if (newEvent.getBelongsToCommunityId() != null) {
            community = communityService.findById(newEvent.getBelongsToCommunityId());
            if (community == null) {
                logger.error("Community not found with id: " + newEvent.getBelongsToCommunityId());
                return new ResponseEntity<>("Community not found.", HttpStatus.NOT_FOUND);
            }
            if (community.isSuspended()) {
                logger.error("Community with id: " + community.getId() + " is suspended");
                return new ResponseEntity<>("This community is suspended.", HttpStatus.FORBIDDEN);
            }
            if (bannedService.isBannedFromCommunity(user.getId(), community.getId())) {
                logger.error("User with id: " + user.getId() + " is banned from community with id: " + community.getId());
                return new ResponseEntity<>("You are banned from this community.", HttpStatus.FORBIDDEN);
            }
            if (!communityService.checkOrganizer(community.getId(), user.getId()) && !user.isAdmin()) {
                logger.error("User with id: " + user.getId() + " is not an organizer of community with id: " + community.getId());
                return new ResponseEntity<>("Only an organizer can create events in this community.", HttpStatus.FORBIDDEN);
            }
        }
        logger.info("Creating event from DTO");
        Event createdEvent = eventService.createEvent(newEvent, user);
        // Slike su vec otpremljene, ovde se samo vezuju za dogadjaj cim on dobije id
        if (newEvent.getImages() != null) {
            for (ImageDTO imageDTO : newEvent.getImages()) {
                if (imageDTO.getPath() != null && !imageDTO.getPath().isBlank())
                    imageService.createEventImage(imageDTO.getPath(), createdEvent);
            }
        }
        if (community != null) {
            logger.info("Adding event with id: " + createdEvent.getId() + " to community with id: " + community.getId());
            eventService.addEventToCommunity(community.getId(), createdEvent.getId());
        }
        logger.info("Created and sent response");

        return new ResponseEntity<>(toDTO(createdEvent), HttpStatus.CREATED);
    }

    @PatchMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> editEvent(@PathVariable String id, @RequestBody EventDTO editedEvent,
                                       @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        logger.info("Finding original event with id: " + id);
        Event oldEvent = eventService.findById(Long.parseLong(id));
        if (oldEvent == null) {
            logger.error("Original event not found with id: " + id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!oldEvent.getCreatedBy().getId().equals(user.getId()) && !user.isAdmin()) {
            logger.error("User with id: " + user.getId() + " is not allowed to edit event with id: " + id);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        // Izmena je delimicna, ali prazna vrednost nije dozvoljena
        if ((editedEvent.getTitle() != null && editedEvent.getTitle().isBlank())
                || (editedEvent.getDescription() != null && editedEvent.getDescription().isBlank())
                || (editedEvent.getLocation() != null && editedEvent.getLocation().isBlank())) {
            logger.error("Event title, description and location cannot be blank");
            return new ResponseEntity<>("Title, description and location cannot be blank.", HttpStatus.BAD_REQUEST);
        }
        // Dogadjaj koji je poceo je vec istorija: ljudi su dosli po objavljenom terminu
        // i po njemu je zabelezen dolazak, pa se termin ne prepravlja unazad
        boolean alreadyStarted = oldEvent.getStartsAt().isBefore(LocalDateTime.now());
        if (alreadyStarted) {
            boolean movesStart = editedEvent.getStartsAt() != null
                    && !LocalDateTime.parse(editedEvent.getStartsAt()).equals(oldEvent.getStartsAt());
            boolean movesEnd = editedEvent.getEndsAt() != null
                    && !LocalDateTime.parse(editedEvent.getEndsAt()).equals(oldEvent.getEndsAt());
            if (movesStart || movesEnd) {
                logger.error("Event with id: " + id + " has already started, its time cannot be changed");
                return new ResponseEntity<>("This event has already started, so its time cannot be changed.",
                        HttpStatus.BAD_REQUEST);
            }
        } else if (editedEvent.getStartsAt() != null
                && LocalDateTime.parse(editedEvent.getStartsAt()).isBefore(LocalDateTime.now())) {
            logger.error("Event with id: " + id + " cannot be moved into the past");
            return new ResponseEntity<>("An event cannot be moved into the past.", HttpStatus.BAD_REQUEST);
        }

        logger.info("Applying changes of event");
        if (editedEvent.getTitle() != null)
            oldEvent.setTitle(editedEvent.getTitle());
        if (editedEvent.getDescription() != null)
            oldEvent.setDescription(editedEvent.getDescription());
        if (editedEvent.getLocation() != null)
            oldEvent.setLocation(editedEvent.getLocation());
        if (editedEvent.getStartsAt() != null)
            oldEvent.setStartsAt(LocalDateTime.parse(editedEvent.getStartsAt()));
        if (editedEvent.getEndsAt() != null)
            oldEvent.setEndsAt(LocalDateTime.parse(editedEvent.getEndsAt()));
        if (!oldEvent.getEndsAt().isAfter(oldEvent.getStartsAt())) {
            logger.error("Event cannot end before it starts");
            return new ResponseEntity<>("An event must end after it starts.", HttpStatus.BAD_REQUEST);
        }
        if (editedEvent.getCategory() != null) {
            EventCategory category = parseCategory(editedEvent.getCategory());
            if (category == null) {
                logger.error("Unknown event category: " + editedEvent.getCategory());
                return new ResponseEntity<>("Unknown event category.", HttpStatus.BAD_REQUEST);
            }
            oldEvent.setCategory(category);
        }
        if (editedEvent.getCapacity() != null) {
            if (editedEvent.getCapacity() < 1) {
                logger.error("Event capacity must be at least one");
                return new ResponseEntity<>("Capacity must be at least one.", HttpStatus.BAD_REQUEST);
            }
            // Kapacitet ne sme da padne ispod broja onih koji vec imaju potvrdjeno mesto,
            // inace bi na dogadjaj dolazilo vise ljudi nego sto ima mesta
            Integer taken = registrationService.countTakenSpots(oldEvent.getId());
            if (editedEvent.getCapacity() < taken) {
                logger.error("Event with id: " + id + " already has " + taken + " taken spots");
                return new ResponseEntity<>("There are already " + taken
                        + " people with a place, so the capacity cannot be lower than that.",
                        HttpStatus.BAD_REQUEST);
            }
            oldEvent.setCapacity(editedEvent.getCapacity());
        }
        oldEvent = eventService.updateEvent(oldEvent);
        // Ako su poslate slike, one zamenjuju postojece
        if (editedEvent.getImages() != null) {
            imageService.deleteImagesForEvent(oldEvent.getId());
            for (ImageDTO imageDTO : editedEvent.getImages()) {
                if (imageDTO.getPath() != null && !imageDTO.getPath().isBlank())
                    imageService.createEventImage(imageDTO.getPath(), oldEvent);
            }
        }
        logger.info("Created and sent response");

        return new ResponseEntity<>(toDTO(oldEvent), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> deleteEvent(@PathVariable String id, @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Event event = eventService.findById(Long.parseLong(id));
        if (event == null) {
            logger.error("Event not found with id: " + id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!event.getCreatedBy().getId().equals(user.getId()) && !user.isAdmin()) {
            logger.error("User with id: " + user.getId() + " is not allowed to delete event with id: " + id);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        logger.info("Deleting event with id: " + id);
        eventService.deleteEventWithContent(event.getId());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private EventCategory parseCategory(String value) {
        if (value == null)
            return null;
        try {
            return EventCategory.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    // Zajednica kojoj dogadjaj pripada se cuva u spojnoj tabeli, pa se dopisuje u DTO posebno
    private EventDTO toDTO(Event event) {
        EventDTO eventDTO = new EventDTO(event);
        eventDTO.setBelongsToCommunityId(eventService.findCommunityIdForEvent(event.getId()));
        eventDTO.setTakenSpots(registrationService.countTakenSpots(event.getId()));
        List<ImageDTO> imageDTOS = new ArrayList<>();
        for (Image image : imageService.findImagesForEvent(event.getId())) {
            imageDTOS.add(new ImageDTO(image));
        }
        eventDTO.setImages(imageDTOS);
        return eventDTO;
    }

    private List<EventDTO> toDTOs(List<Event> events) {
        List<EventDTO> eventDTOS = new ArrayList<>();
        for (Event temp : events) {
            eventDTOS.add(toDTO(temp));
        }
        return eventDTOS;
    }

    private User findUserByToken(String token) {
        String cleanToken = token.substring(7);
        String username = tokenUtils.getUsernameFromToken(cleanToken);
        return userService.findByUsername(username);
    }
}

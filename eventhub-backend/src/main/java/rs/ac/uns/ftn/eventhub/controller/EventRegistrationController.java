package rs.ac.uns.ftn.eventhub.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.eventhub.model.dto.EventRegistrationDTO;
import rs.ac.uns.ftn.eventhub.model.entity.Event;
import rs.ac.uns.ftn.eventhub.model.entity.EventRegistration;
import rs.ac.uns.ftn.eventhub.model.entity.User;
import rs.ac.uns.ftn.eventhub.model.enums.RegistrationStatus;
import rs.ac.uns.ftn.eventhub.security.TokenUtils;
import rs.ac.uns.ftn.eventhub.service.CommunityService;
import rs.ac.uns.ftn.eventhub.service.EventRegistrationService;
import rs.ac.uns.ftn.eventhub.service.EventService;
import rs.ac.uns.ftn.eventhub.service.UserService;
import rs.ac.uns.ftn.eventhub.service.implementation.CommunityServiceImpl;
import rs.ac.uns.ftn.eventhub.service.implementation.EventRegistrationServiceImpl;
import rs.ac.uns.ftn.eventhub.service.implementation.EventServiceImpl;
import rs.ac.uns.ftn.eventhub.service.implementation.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("api/registrations")
public class EventRegistrationController {


    EventRegistrationService registrationService;


    EventService eventService;


    CommunityService communityService;


    UserService userService;


    TokenUtils tokenUtils;

    private static final Logger logger = LogManager.getLogger(EventRegistrationController.class);

    @Autowired
    public EventRegistrationController(EventRegistrationServiceImpl registrationService, EventServiceImpl eventService,
                                       CommunityServiceImpl communityService, UserServiceImpl userService, TokenUtils tokenUtils) {
        this.registrationService = registrationService;
        this.eventService = eventService;
        this.communityService = communityService;
        this.userService = userService;
        this.tokenUtils = tokenUtils;
    }

    @PostMapping("/event/{eventId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> register(@PathVariable String eventId, @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Event event = eventService.findById(Long.parseLong(eventId));
        if (event == null) {
            logger.error("Event not found with id: " + eventId);
            return new ResponseEntity<>("Event not found.", HttpStatus.NOT_FOUND);
        }
        if (event.getStartsAt().isBefore(LocalDateTime.now())) {
            logger.error("Event with id: " + eventId + " has already started");
            return new ResponseEntity<>("This event has already started.", HttpStatus.BAD_REQUEST);
        }
        if (registrationService.findActiveRegistration(user.getId(), event.getId()) != null) {
            logger.error("User with id: " + user.getId() + " is already registered for event with id: " + eventId);
            return new ResponseEntity<>("You are already registered for this event.", HttpStatus.CONFLICT);
        }
        if (registrationService.countTakenSpots(event.getId()) >= event.getCapacity()) {
            logger.error("Event with id: " + eventId + " is full");
            return new ResponseEntity<>("This event is full.", HttpStatus.CONFLICT);
        }
        logger.info("Creating registration of user with id: " + user.getId() + " for event with id: " + eventId);
        EventRegistration registration = registrationService.createRegistration(user, event, RegistrationStatus.PENDING);
        logger.info("Created and sent response");

        return new ResponseEntity<>(new EventRegistrationDTO(registration), HttpStatus.CREATED);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<EventRegistrationDTO>> getMyRegistrations(@RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        logger.info("Finding registrations of user with id: " + user.getId());
        logger.info("Created and sent response");

        return new ResponseEntity<>(toDTOs(registrationService.findRegistrationsForUser(user.getId())), HttpStatus.OK);
    }

    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<EventRegistrationDTO>> getRegistrationsForEvent(@PathVariable String eventId,
                                                                              @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Event event = eventService.findById(Long.parseLong(eventId));
        if (event == null) {
            logger.error("Event not found with id: " + eventId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!canManage(user, event)) {
            logger.error("User with id: " + user.getId() + " is not allowed to see registrations for event with id: " + eventId);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        logger.info("Finding registrations for event with id: " + eventId);
        logger.info("Created and sent response");

        return new ResponseEntity<>(toDTOs(registrationService.findRegistrationsForEvent(event.getId())), HttpStatus.OK);
    }

    @PatchMapping("/{id}/accept")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> accept(@PathVariable String id, @RequestHeader("authorization") String token) {
        return decide(id, token, RegistrationStatus.ACCEPTED);
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> reject(@PathVariable String id, @RequestHeader("authorization") String token) {
        return decide(id, token, RegistrationStatus.REJECTED);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> cancel(@PathVariable String id, @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        EventRegistration registration = registrationService.findById(Long.parseLong(id));
        if (registration == null) {
            logger.error("Registration not found with id: " + id);
            return new ResponseEntity<>("Registration not found.", HttpStatus.NOT_FOUND);
        }
        if (!registration.getCreatedBy().getId().equals(user.getId())) {
            logger.error("User with id: " + user.getId() + " is not allowed to cancel registration with id: " + id);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if (registration.getStatus() == RegistrationStatus.CANCELLED) {
            logger.error("Registration with id: " + id + " is already cancelled");
            return new ResponseEntity<>("This registration is already cancelled.", HttpStatus.CONFLICT);
        }
        logger.info("Cancelling registration with id: " + id);
        registration = registrationService.updateStatus(registration, RegistrationStatus.CANCELLED);

        return new ResponseEntity<>(new EventRegistrationDTO(registration), HttpStatus.OK);
    }

    // Prihvatanje i odbijanje se razlikuju samo po statusu, pa dele istu proveru
    private ResponseEntity<?> decide(String id, String token, RegistrationStatus status) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        EventRegistration registration = registrationService.findById(Long.parseLong(id));
        if (registration == null) {
            logger.error("Registration not found with id: " + id);
            return new ResponseEntity<>("Registration not found.", HttpStatus.NOT_FOUND);
        }
        // Dogadjaj se dohvata iz servisa, jer je veza lazy a sesija je vec zatvorena (open-in-view=false)
        Event event = eventService.findById(registration.getForEvent().getId());
        if (event == null) {
            logger.error("Event not found for registration with id: " + id);
            return new ResponseEntity<>("Event not found.", HttpStatus.NOT_FOUND);
        }
        if (!canManage(user, event)) {
            logger.error("User with id: " + user.getId() + " is not allowed to decide on registration with id: " + id);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if (registration.getStatus() == RegistrationStatus.CANCELLED) {
            logger.error("Registration with id: " + id + " was cancelled by the user");
            return new ResponseEntity<>("This registration was cancelled by the user.", HttpStatus.CONFLICT);
        }
        if (status == RegistrationStatus.ACCEPTED
                && registrationService.countTakenSpots(event.getId()) >= event.getCapacity()) {
            logger.error("Event with id: " + event.getId() + " is full");
            return new ResponseEntity<>("This event is full.", HttpStatus.CONFLICT);
        }
        registration = registrationService.updateStatus(registration, status);

        return new ResponseEntity<>(new EventRegistrationDTO(registration), HttpStatus.OK);
    }

    // Prijavama upravlja onaj ko je napravio dogadjaj, organizator zajednice kojoj dogadjaj pripada, ili administrator
    private boolean canManage(User user, Event event) {
        if (user.isAdmin())
            return true;
        if (event.getCreatedBy().getId().equals(user.getId()))
            return true;
        Long communityId = eventService.findCommunityIdForEvent(event.getId());
        return communityId != null && communityService.checkOrganizer(communityId, user.getId());
    }

    private List<EventRegistrationDTO> toDTOs(List<EventRegistration> registrations) {
        List<EventRegistrationDTO> dtos = new ArrayList<>();
        for (EventRegistration temp : registrations) {
            dtos.add(new EventRegistrationDTO(temp));
        }
        return dtos;
    }

    private User findUserByToken(String token) {
        String cleanToken = token.substring(7);
        String username = tokenUtils.getUsernameFromToken(cleanToken);
        return userService.findByUsername(username);
    }
}

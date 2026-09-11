package rs.ac.uns.ftn.eventhub.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.eventhub.model.dto.BannedDTO;
import rs.ac.uns.ftn.eventhub.model.entity.Banned;
import rs.ac.uns.ftn.eventhub.model.entity.Community;
import rs.ac.uns.ftn.eventhub.model.entity.Event;
import rs.ac.uns.ftn.eventhub.model.entity.EventRegistration;
import rs.ac.uns.ftn.eventhub.model.entity.User;
import rs.ac.uns.ftn.eventhub.model.enums.RegistrationStatus;
import rs.ac.uns.ftn.eventhub.security.TokenUtils;
import rs.ac.uns.ftn.eventhub.service.BannedService;
import rs.ac.uns.ftn.eventhub.service.CommunityService;
import rs.ac.uns.ftn.eventhub.service.EventRegistrationService;
import rs.ac.uns.ftn.eventhub.service.EventService;
import rs.ac.uns.ftn.eventhub.service.UserService;
import rs.ac.uns.ftn.eventhub.service.implementation.BannedServiceImpl;
import rs.ac.uns.ftn.eventhub.service.implementation.CommunityServiceImpl;
import rs.ac.uns.ftn.eventhub.service.implementation.EventRegistrationServiceImpl;
import rs.ac.uns.ftn.eventhub.service.implementation.EventServiceImpl;
import rs.ac.uns.ftn.eventhub.service.implementation.UserServiceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("api/bans")
public class BannedController {


    BannedService bannedService;


    CommunityService communityService;


    EventService eventService;


    EventRegistrationService registrationService;


    UserService userService;


    TokenUtils tokenUtils;

    private static final Logger logger = LogManager.getLogger(BannedController.class);

    @Autowired
    public BannedController(BannedServiceImpl bannedService, CommunityServiceImpl communityService,
                            EventServiceImpl eventService, EventRegistrationServiceImpl registrationService,
                            UserServiceImpl userService, TokenUtils tokenUtils) {
        this.bannedService = bannedService;
        this.communityService = communityService;
        this.eventService = eventService;
        this.registrationService = registrationService;
        this.userService = userService;
        this.tokenUtils = tokenUtils;
    }

    // Blokadu na nivou sistema izrice samo administrator

    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BannedDTO>> getSystemBans(@RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        logger.info("Finding all bans, both from the system and from communities");

        return new ResponseEntity<>(toDTOs(bannedService.findAllBans()), HttpStatus.OK);
    }

    @PostMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> banFromSystem(@PathVariable String userId, @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User admin = findUserByToken(token);
        if (admin == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        User target = userService.findById(Long.parseLong(userId));
        if (target == null) {
            logger.error("User not found with id: " + userId);
            return new ResponseEntity<>("User not found.", HttpStatus.NOT_FOUND);
        }
        if (target.getId().equals(admin.getId())) {
            logger.error("Administrator tried to ban themselves");
            return new ResponseEntity<>("You cannot ban yourself.", HttpStatus.BAD_REQUEST);
        }
        if (bannedService.isBannedFromSystem(target.getId())) {
            logger.error("User with id: " + userId + " is already banned from the system");
            return new ResponseEntity<>("This user is already banned.", HttpStatus.CONFLICT);
        }
        Banned banned = bannedService.ban(admin, target, null);

        return new ResponseEntity<>(new BannedDTO(banned), HttpStatus.CREATED);
    }

    // Blokadu u zajednici izrice njen organizator

    @GetMapping("/community/{communityId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<BannedDTO>> getCommunityBans(@PathVariable String communityId,
                                                            @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!communityService.checkOrganizer(Long.parseLong(communityId), user.getId()) && !user.isAdmin()) {
            logger.error("User with id: " + user.getId() + " is not an organizer of community with id: " + communityId);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        logger.info("Finding users banned from community with id: " + communityId);

        return new ResponseEntity<>(toDTOs(bannedService.findBansForCommunity(Long.parseLong(communityId))), HttpStatus.OK);
    }

    @PostMapping("/community/{communityId}/user/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> banFromCommunity(@PathVariable String communityId, @PathVariable String userId,
                                              @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Community community = communityService.findById(Long.parseLong(communityId));
        if (community == null) {
            logger.error("Community not found with id: " + communityId);
            return new ResponseEntity<>("Community not found.", HttpStatus.NOT_FOUND);
        }
        if (!communityService.checkOrganizer(community.getId(), user.getId()) && !user.isAdmin()) {
            logger.error("User with id: " + user.getId() + " is not an organizer of community with id: " + communityId);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        User target = userService.findById(Long.parseLong(userId));
        if (target == null) {
            logger.error("User not found with id: " + userId);
            return new ResponseEntity<>("User not found.", HttpStatus.NOT_FOUND);
        }
        // Organizator ne moze da blokira drugog organizatora te zajednice
        if (communityService.checkOrganizer(community.getId(), target.getId())) {
            logger.error("User with id: " + userId + " is an organizer of community with id: " + communityId);
            return new ResponseEntity<>("You cannot ban an organizer of this community.", HttpStatus.FORBIDDEN);
        }
        if (bannedService.isBannedFromCommunity(target.getId(), community.getId())) {
            logger.error("User with id: " + userId + " is already banned from community with id: " + communityId);
            return new ResponseEntity<>("This user is already banned from this community.", HttpStatus.CONFLICT);
        }
        Banned banned = bannedService.ban(user, target, community);
        // Blokiran korisnik prestaje da bude clan zajednice
        communityService.deleteCommunityMember(community.getId(), target.getId());
        // I ne ostaje na spisku ucesnika njenih dogadjaja koji tek predstoje
        cancelUpcomingRegistrations(community, target);

        return new ResponseEntity<>(new BannedDTO(banned), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> unban(@PathVariable String id, @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Banned banned = bannedService.findById(Long.parseLong(id));
        if (banned == null) {
            logger.error("Ban not found with id: " + id);
            return new ResponseEntity<>("Ban not found.", HttpStatus.NOT_FOUND);
        }
        // Blokadu na nivou sistema skida samo administrator, a onu u zajednici njen organizator
        boolean systemBan = banned.getCommunity() == null;
        if (systemBan) {
            if (!user.isAdmin()) {
                logger.error("User with id: " + user.getId() + " is not allowed to remove a system ban");
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }
        } else if (!communityService.checkOrganizer(banned.getCommunity().getId(), user.getId()) && !user.isAdmin()) {
            logger.error("User with id: " + user.getId() + " is not allowed to remove this ban");
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        bannedService.unban(banned.getId());

        return new ResponseEntity<>("User unbanned.", HttpStatus.OK);
    }


    // Blokada vazi i unapred: prijave na buduce dogadjaje zajednice se otkazuju,
    // a oslobodjeno mesto pripada prvom sa liste cekanja
    private void cancelUpcomingRegistrations(Community community, User target) {
        for (Event event : eventService.findEventsForCommunity(community.getId())) {
            if (event.getStartsAt().isBefore(LocalDateTime.now()))
                continue;
            EventRegistration registration = registrationService.findActiveRegistration(target.getId(), event.getId());
            if (registration == null)
                continue;

            boolean freedSpot = registration.getStatus() == RegistrationStatus.ACCEPTED;
            logger.info("Cancelling registration with id: " + registration.getId()
                    + " of banned user with id: " + target.getId());
            registrationService.updateStatus(registration, RegistrationStatus.CANCELLED);
            if (freedSpot)
                registrationService.promoteFromWaitlist(event);
        }
    }

    private List<BannedDTO> toDTOs(List<Banned> bans) {
        List<BannedDTO> dtos = new ArrayList<>();
        for (Banned temp : bans) {
            BannedDTO dto = new BannedDTO(temp);
            User target = userService.findById(temp.getTowardsUser().getId());
            if (target != null)
                dto.setTowardsUsername(target.getUsername());
            if (temp.getCommunity() != null) {
                Community community = communityService.findById(temp.getCommunity().getId());
                if (community != null)
                    dto.setCommunityName(community.getName());
            }
            dtos.add(dto);
        }
        return dtos;
    }

    private User findUserByToken(String token) {
        String cleanToken = token.substring(7);
        String username = tokenUtils.getUsernameFromToken(cleanToken);
        return userService.findByUsername(username);
    }
}

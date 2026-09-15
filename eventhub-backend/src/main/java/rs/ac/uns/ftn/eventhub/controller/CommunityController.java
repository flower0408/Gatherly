package rs.ac.uns.ftn.eventhub.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.eventhub.model.dto.CommunityDTO;
import rs.ac.uns.ftn.eventhub.model.dto.ImageDTO;
import rs.ac.uns.ftn.eventhub.model.dto.UserDTO;
import rs.ac.uns.ftn.eventhub.model.entity.Community;
import rs.ac.uns.ftn.eventhub.model.entity.Event;
import rs.ac.uns.ftn.eventhub.model.entity.Image;
import rs.ac.uns.ftn.eventhub.model.entity.User;
import rs.ac.uns.ftn.eventhub.security.TokenUtils;
import rs.ac.uns.ftn.eventhub.service.BannedService;
import rs.ac.uns.ftn.eventhub.service.CommunityService;
import rs.ac.uns.ftn.eventhub.service.EventService;
import rs.ac.uns.ftn.eventhub.service.ImageService;
import rs.ac.uns.ftn.eventhub.service.UserService;
import rs.ac.uns.ftn.eventhub.service.implementation.BannedServiceImpl;
import rs.ac.uns.ftn.eventhub.service.implementation.CommunityServiceImpl;
import rs.ac.uns.ftn.eventhub.service.implementation.EventServiceImpl;
import rs.ac.uns.ftn.eventhub.service.implementation.ImageServiceImpl;
import rs.ac.uns.ftn.eventhub.service.implementation.UserServiceImpl;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("api/communities")
public class CommunityController {


    CommunityService communityService;


    UserService userService;


    ImageService imageService;


    EventService eventService;


    BannedService bannedService;


    TokenUtils tokenUtils;

    private static final Logger logger = LogManager.getLogger(CommunityController.class);

    @Autowired
    public CommunityController(CommunityServiceImpl communityService, UserServiceImpl userService,
                               ImageServiceImpl imageService, EventServiceImpl eventService,
                               BannedServiceImpl bannedService, TokenUtils tokenUtils) {
        this.communityService = communityService;
        this.userService = userService;
        this.imageService = imageService;
        this.eventService = eventService;
        this.bannedService = bannedService;
        this.tokenUtils = tokenUtils;
    }

    // Rute za pregled su otvorene i za goste, zato ne traze token

    @GetMapping()
    public ResponseEntity<List<CommunityDTO>> getAll() {
        logger.info("Finding all communities");
        List<CommunityDTO> communityDTOS = new ArrayList<>();
        for (Community temp : communityService.findAll()) {
            communityDTOS.add(new CommunityDTO(temp));
        }
        logger.info("Created and sent response");

        return new ResponseEntity<>(communityDTOS, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommunityDTO> getOne(@PathVariable String id) {
        logger.info("Finding community with id: " + id);
        Community community = communityService.findById(Long.parseLong(id));
        if (community == null) {
            logger.error("Community not found with id: " + id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        logger.info("Created and sent response");

        return new ResponseEntity<>(new CommunityDTO(community), HttpStatus.OK);
    }

    @GetMapping("/members/{communityId}")
    public ResponseEntity<List<UserDTO>> getCommunityMembers(@PathVariable String communityId) {
        logger.info("Finding members of community with id: " + communityId);
        List<UserDTO> userDTOS = new ArrayList<>();
        for (Long memberId : communityService.findMembersByCommunityId(Long.parseLong(communityId))) {
            User member = userService.findById(memberId);
            if (member != null)
                userDTOS.add(toDTO(member));
        }
        logger.info("Created and sent response");

        return new ResponseEntity<>(userDTOS, HttpStatus.OK);
    }

    @GetMapping("/organizers/{communityId}")
    public ResponseEntity<List<UserDTO>> getCommunityOrganizers(@PathVariable String communityId) {
        logger.info("Finding organizers of community with id: " + communityId);
        List<UserDTO> userDTOS = new ArrayList<>();
        for (Long organizerId : communityService.findOrganizersByCommunityId(Long.parseLong(communityId))) {
            User organizer = userService.findById(organizerId);
            if (organizer != null)
                userDTOS.add(toDTO(organizer));
        }
        logger.info("Created and sent response");

        return new ResponseEntity<>(userDTOS, HttpStatus.OK);
    }

    // Zajednice kojima jedan korisnik pripada, za prikaz na njegovom profilu
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CommunityDTO>> getCommunitiesOfUser(@PathVariable String userId) {
        logger.info("Finding communities of user with id: " + userId);
        List<CommunityDTO> communityDTOS = new ArrayList<>();
        for (Community temp : communityService.findCommunitiesForUser(Long.parseLong(userId))) {
            communityDTOS.add(new CommunityDTO(temp));
        }
        logger.info("Created and sent response");

        return new ResponseEntity<>(communityDTOS, HttpStatus.OK);
    }

    // Od ove tacke rute traze prijavljenog korisnika

    // Zajednice u kojima korisnik moze da otvara dogadjaje, dakle one gde je organizator
    @GetMapping("/my/organizing")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<CommunityDTO>> getCommunitiesIOrganize(@RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        logger.info("Finding communities organized by user with id: " + user.getId());
        List<CommunityDTO> communityDTOS = new ArrayList<>();
        for (Community temp : communityService.findCommunitiesForOrganizer(user.getId())) {
            communityDTOS.add(new CommunityDTO(temp));
        }
        logger.info("Created and sent response");

        return new ResponseEntity<>(communityDTOS, HttpStatus.OK);
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<CommunityDTO> createCommunity(@RequestBody @Validated CommunityDTO newCommunity,
                                                        @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        logger.info("Creating community from DTO");
        Community createdCommunity = communityService.createCommunity(newCommunity);
        if (createdCommunity == null) {
            logger.error("Community couldn't be created from DTO");
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        // Onaj ko je napravio zajednicu postaje njen organizator i clan
        logger.info("Setting user with id: " + user.getId() + " as organizer and member");
        communityService.addCommunityOrganizer(createdCommunity.getId(), user.getId());
        communityService.addCommunityMember(createdCommunity.getId(), user.getId());
        logger.info("Created and sent response");

        return new ResponseEntity<>(new CommunityDTO(createdCommunity), HttpStatus.CREATED);
    }

    @PatchMapping("/edit/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<CommunityDTO> editCommunity(@PathVariable String id, @RequestBody CommunityDTO editedCommunity,
                                                      @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        logger.info("Finding original community with id: " + id);
        Community oldCommunity = communityService.findById(Long.parseLong(id));
        if (oldCommunity == null) {
            logger.error("Original community not found with id: " + id);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!communityService.checkOrganizer(oldCommunity.getId(), user.getId()) && !user.isAdmin()) {
            logger.error("User with id: " + user.getId() + " is not allowed to edit community with id: " + id);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        // Izmena je delimicna, salju se samo polja koja se menjaju, ali prazna vrednost nije dozvoljena
        if ((editedCommunity.getName() != null && editedCommunity.getName().isBlank())
                || (editedCommunity.getDescription() != null && editedCommunity.getDescription().isBlank())) {
            logger.error("Community name and description cannot be blank");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        logger.info("Applying changes of community");
        if (editedCommunity.getName() != null)
            oldCommunity.setName(editedCommunity.getName());
        if (editedCommunity.getDescription() != null)
            oldCommunity.setDescription(editedCommunity.getDescription());
        oldCommunity = communityService.updateCommunity(oldCommunity);
        logger.info("Created and sent response");

        return new ResponseEntity<>(new CommunityDTO(oldCommunity), HttpStatus.OK);
    }

    @PatchMapping("/suspend/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommunityDTO> suspendCommunity(@PathVariable String id, @RequestBody CommunityDTO suspendRequest,
                                                         @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        logger.info("Finding community with id: " + id);
        Community community = communityService.findById(Long.parseLong(id));
        if (community == null) {
            logger.error("Community not found with id: " + id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (suspendRequest.getSuspendedReason() == null || suspendRequest.getSuspendedReason().isBlank()) {
            logger.error("Suspension must have a reason");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        logger.info("Suspending community with id: " + id);
        community.setSuspended(true);
        community.setSuspendedReason(suspendRequest.getSuspendedReason());
        community = communityService.updateCommunity(community);
        // Suspendovana zajednica ostaje bez organizatora
        communityService.deleteCommunityOrganizers(community.getId());
        logger.info("Created and sent response");

        return new ResponseEntity<>(new CommunityDTO(community), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> deleteCommunity(@PathVariable String id, @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Community community = communityService.findById(Long.parseLong(id));
        if (community == null) {
            logger.error("Community not found with id: " + id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!communityService.checkOrganizer(community.getId(), user.getId()) && !user.isAdmin()) {
            logger.error("User with id: " + user.getId() + " is not allowed to delete community with id: " + id);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        // Sa zajednicom odlaze i njeni dogadjaji, jer bez nje ne bi imali ko da ih vodi
        logger.info("Deleting events of community with id: " + id);
        for (Event event : eventService.findEventsForCommunity(community.getId())) {
            eventService.deleteEventWithContent(event.getId());
        }
        logger.info("Deleting community with id: " + id);
        communityService.deleteCommunityOrganizers(community.getId());
        communityService.deleteCommunityMembers(community.getId());
        communityService.deleteCommunity(community.getId());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{communityId}/member")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<String> joinCommunity(@PathVariable String communityId, @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Community community = communityService.findById(Long.parseLong(communityId));
        if (community == null) {
            logger.error("Community not found with id: " + communityId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (community.isSuspended()) {
            logger.error("Community with id: " + communityId + " is suspended and cannot be joined");
            return new ResponseEntity<>("This community is suspended.", HttpStatus.FORBIDDEN);
        }
        if (bannedService.isBannedFromCommunity(user.getId(), community.getId())) {
            logger.error("User with id: " + user.getId() + " is banned from community with id: " + communityId);
            return new ResponseEntity<>("You are banned from this community.", HttpStatus.FORBIDDEN);
        }
        if (communityService.checkMember(community.getId(), user.getId())) {
            logger.error("User with id: " + user.getId() + " is already a member of community with id: " + communityId);
            return new ResponseEntity<>("You are already a member of this community.", HttpStatus.CONFLICT);
        }
        logger.info("Adding user with id: " + user.getId() + " to community with id: " + communityId);
        communityService.addCommunityMember(community.getId(), user.getId());

        return new ResponseEntity<>("You have joined the community.", HttpStatus.OK);
    }

    @DeleteMapping("/{communityId}/member")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<String> leaveCommunity(@PathVariable String communityId, @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        // Organizator koji izadje iz zajednice bi joj i dalje vodio dogadjaje, zato se prvo mora povuci
        if (communityService.checkOrganizer(Long.parseLong(communityId), user.getId())) {
            logger.error("User with id: " + user.getId() + " is still an organizer of community with id: " + communityId);
            return new ResponseEntity<>("Step down as an organizer before leaving this community.", HttpStatus.CONFLICT);
        }
        logger.info("Removing user with id: " + user.getId() + " from community with id: " + communityId);
        Integer removed = communityService.deleteCommunityMember(Long.parseLong(communityId), user.getId());
        if (removed == 0) {
            logger.error("User with id: " + user.getId() + " is not a member of community with id: " + communityId);
            return new ResponseEntity<>("You are not a member of this community.", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>("You have left the community.", HttpStatus.OK);
    }

    @PostMapping("/{communityId}/organizer/{organizerId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<String> addCommunityOrganizer(@PathVariable String communityId, @PathVariable String organizerId,
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
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (community.isSuspended()) {
            logger.error("Community with id: " + communityId + " is suspended");
            return new ResponseEntity<>("This community is suspended.", HttpStatus.FORBIDDEN);
        }
        if (!communityService.checkOrganizer(community.getId(), user.getId()) && !user.isAdmin()) {
            logger.error("User with id: " + user.getId() + " is not allowed to add organizers to community with id: " + communityId);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        User newOrganizer = userService.findById(Long.parseLong(organizerId));
        if (newOrganizer == null) {
            logger.error("User not found with id: " + organizerId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (communityService.checkOrganizer(community.getId(), newOrganizer.getId())) {
            logger.error("User with id: " + organizerId + " is already an organizer of community with id: " + communityId);
            return new ResponseEntity<>("This user is already an organizer.", HttpStatus.CONFLICT);
        }
        logger.info("Adding user with id: " + organizerId + " as organizer of community with id: " + communityId);
        communityService.addCommunityOrganizer(community.getId(), newOrganizer.getId());
        if (!communityService.checkMember(community.getId(), newOrganizer.getId()))
            communityService.addCommunityMember(community.getId(), newOrganizer.getId());

        return new ResponseEntity<>("Organizer added.", HttpStatus.OK);
    }

    // Administrator sme da skine bilo kog organizatora, a organizator sme da povuce jedino sam sebe
    @DeleteMapping("/delete/{communityId}/organizer/{organizerId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<String> deleteCommunityOrganizer(@PathVariable String communityId, @PathVariable String organizerId,
                                                           @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        if (!user.isAdmin() && !user.getId().equals(Long.parseLong(organizerId))) {
            logger.error("User with id: " + user.getId() + " is not allowed to remove organizer with id: " + organizerId);
            return new ResponseEntity<>("Only an administrator can remove another organizer.", HttpStatus.FORBIDDEN);
        }
        // Zajednica bez ijednog organizatora ne bi imala ko da vodi njene dogadjaje
        if (communityService.findOrganizersByCommunityId(Long.parseLong(communityId)).size() <= 1) {
            logger.error("Community with id: " + communityId + " would be left without an organizer");
            return new ResponseEntity<>("A community cannot be left without an organizer.", HttpStatus.CONFLICT);
        }
        logger.info("Removing organizer with id: " + organizerId + " from community with id: " + communityId);
        Integer removed = communityService.deleteCommunityOrganizer(Long.parseLong(communityId), Long.parseLong(organizerId));
        if (removed == 0) {
            logger.error("User with id: " + organizerId + " is not an organizer of community with id: " + communityId);
            return new ResponseEntity<>("This user is not an organizer of this community.", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>("Organizer removed.", HttpStatus.OK);
    }

    // Uz korisnika se salje i njegova profilna slika, da front ne bi za svakog clana slao poseban zahtev
    private UserDTO toDTO(User user) {
        UserDTO userDTO = new UserDTO(user);
        Image profileImage = imageService.findProfileImageForUser(user.getId());
        if (profileImage != null)
            userDTO.setProfileImage(new ImageDTO(profileImage));
        return userDTO;
    }

    // substring(7) skida prefiks "Bearer " iz zaglavlja Authorization
    private User findUserByToken(String token) {
        String cleanToken = token.substring(7);
        String username = tokenUtils.getUsernameFromToken(cleanToken);
        return userService.findByUsername(username);
    }
}

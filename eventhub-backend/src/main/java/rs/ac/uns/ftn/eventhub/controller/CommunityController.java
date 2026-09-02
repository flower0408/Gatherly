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
import rs.ac.uns.ftn.eventhub.model.dto.UserDTO;
import rs.ac.uns.ftn.eventhub.model.entity.Community;
import rs.ac.uns.ftn.eventhub.model.entity.User;
import rs.ac.uns.ftn.eventhub.security.TokenUtils;
import rs.ac.uns.ftn.eventhub.service.CommunityService;
import rs.ac.uns.ftn.eventhub.service.UserService;
import rs.ac.uns.ftn.eventhub.service.implementation.CommunityServiceImpl;
import rs.ac.uns.ftn.eventhub.service.implementation.UserServiceImpl;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("api/communities")
public class CommunityController {


    CommunityService communityService;


    UserService userService;


    TokenUtils tokenUtils;

    private static final Logger logger = LogManager.getLogger(CommunityController.class);

    @Autowired
    public CommunityController(CommunityServiceImpl communityService, UserServiceImpl userService, TokenUtils tokenUtils) {
        this.communityService = communityService;
        this.userService = userService;
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
                userDTOS.add(new UserDTO(member));
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
                userDTOS.add(new UserDTO(organizer));
        }
        logger.info("Created and sent response");

        return new ResponseEntity<>(userDTOS, HttpStatus.OK);
    }

    // Od ove tacke rute traze prijavljenog korisnika

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<CommunityDTO>> getMyCommunities(@RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        logger.info("Finding communities of user with id: " + user.getId());
        List<CommunityDTO> communityDTOS = new ArrayList<>();
        for (Community temp : communityService.findCommunitiesForUser(user.getId())) {
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
            return new ResponseEntity<>(null, HttpStatus.NOT_ACCEPTABLE);
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
    public ResponseEntity<CommunityDTO> editCommunity(@PathVariable String id, @RequestBody @Validated CommunityDTO editedCommunity,
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
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        if (!communityService.checkOrganizer(oldCommunity.getId(), user.getId()) && !user.isAdmin()) {
            logger.error("User with id: " + user.getId() + " is not allowed to edit community with id: " + id);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
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
    public ResponseEntity<CommunityDTO> suspendCommunity(@PathVariable String id, @RequestBody @Validated CommunityDTO suspendRequest,
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

    @DeleteMapping("/delete/{communityId}/organizer/{organizerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteCommunityOrganizer(@PathVariable String communityId, @PathVariable String organizerId,
                                                           @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        logger.info("Removing organizer with id: " + organizerId + " from community with id: " + communityId);
        Integer removed = communityService.deleteCommunityOrganizer(Long.parseLong(communityId), Long.parseLong(organizerId));
        if (removed == 0) {
            logger.error("User with id: " + organizerId + " is not an organizer of community with id: " + communityId);
            return new ResponseEntity<>("This user is not an organizer of this community.", HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>("Organizer removed.", HttpStatus.OK);
    }

    // substring(7) skida prefiks "Bearer " iz zaglavlja Authorization
    private User findUserByToken(String token) {
        String cleanToken = token.substring(7);
        String username = tokenUtils.getUsernameFromToken(cleanToken);
        return userService.findByUsername(username);
    }
}

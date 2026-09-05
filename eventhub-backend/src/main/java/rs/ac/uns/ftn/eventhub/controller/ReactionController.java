package rs.ac.uns.ftn.eventhub.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.eventhub.model.dto.ReactionDTO;
import rs.ac.uns.ftn.eventhub.model.entity.Comment;
import rs.ac.uns.ftn.eventhub.model.entity.Event;
import rs.ac.uns.ftn.eventhub.model.entity.Reaction;
import rs.ac.uns.ftn.eventhub.model.entity.User;
import rs.ac.uns.ftn.eventhub.model.enums.ReactionType;
import rs.ac.uns.ftn.eventhub.security.TokenUtils;
import rs.ac.uns.ftn.eventhub.service.CommentService;
import rs.ac.uns.ftn.eventhub.service.EventService;
import rs.ac.uns.ftn.eventhub.service.ReactionService;
import rs.ac.uns.ftn.eventhub.service.UserService;
import rs.ac.uns.ftn.eventhub.service.implementation.CommentServiceImpl;
import rs.ac.uns.ftn.eventhub.service.implementation.EventServiceImpl;
import rs.ac.uns.ftn.eventhub.service.implementation.ReactionServiceImpl;
import rs.ac.uns.ftn.eventhub.service.implementation.UserServiceImpl;

import java.util.Map;


@RestController
@RequestMapping("api/reactions")
public class ReactionController {


    ReactionService reactionService;


    EventService eventService;


    CommentService commentService;


    UserService userService;


    TokenUtils tokenUtils;

    private static final Logger logger = LogManager.getLogger(ReactionController.class);

    @Autowired
    public ReactionController(ReactionServiceImpl reactionService, EventServiceImpl eventService,
                              CommentServiceImpl commentService, UserServiceImpl userService, TokenUtils tokenUtils) {
        this.reactionService = reactionService;
        this.eventService = eventService;
        this.commentService = commentService;
        this.userService = userService;
        this.tokenUtils = tokenUtils;
    }

    // Broj reakcija se vidi i bez prijave, jer su stranice dogadjaja javne

    @GetMapping("/event/{eventId}")
    public ResponseEntity<Map<String, Integer>> countsForEvent(@PathVariable String eventId) {
        logger.info("Counting reactions for event with id: " + eventId);

        return new ResponseEntity<>(reactionService.countsForEvent(Long.parseLong(eventId)), HttpStatus.OK);
    }

    @GetMapping("/comment/{commentId}")
    public ResponseEntity<Map<String, Integer>> countsForComment(@PathVariable String commentId) {
        logger.info("Counting reactions for comment with id: " + commentId);

        return new ResponseEntity<>(reactionService.countsForComment(Long.parseLong(commentId)), HttpStatus.OK);
    }

    // Od ove tacke rute traze prijavljenog korisnika

    @PostMapping("/event/{eventId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> reactToEvent(@PathVariable String eventId, @RequestBody ReactionDTO reactionDTO,
                                          @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        ReactionType type = parseType(reactionDTO.getReactionType());
        if (type == null) {
            logger.error("Unknown reaction type: " + reactionDTO.getReactionType());
            return new ResponseEntity<>("Reaction must be one of LIKE, DISLIKE, HEART.", HttpStatus.BAD_REQUEST);
        }
        Event event = eventService.findById(Long.parseLong(eventId));
        if (event == null) {
            logger.error("Event not found with id: " + eventId);
            return new ResponseEntity<>("Event not found.", HttpStatus.NOT_FOUND);
        }
        Reaction reaction = reactionService.reactToEvent(user, event, type);
        // Kada se ista reakcija ponovi, ona se povlaci i nema sta da se vrati
        if (reaction == null)
            return new ResponseEntity<>(reactionService.countsForEvent(event.getId()), HttpStatus.OK);

        return new ResponseEntity<>(new ReactionDTO(reaction), HttpStatus.OK);
    }

    @PostMapping("/comment/{commentId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> reactToComment(@PathVariable String commentId, @RequestBody ReactionDTO reactionDTO,
                                            @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        ReactionType type = parseType(reactionDTO.getReactionType());
        if (type == null) {
            logger.error("Unknown reaction type: " + reactionDTO.getReactionType());
            return new ResponseEntity<>("Reaction must be one of LIKE, DISLIKE, HEART.", HttpStatus.BAD_REQUEST);
        }
        Comment comment = commentService.findById(Long.parseLong(commentId));
        if (comment == null) {
            logger.error("Comment not found with id: " + commentId);
            return new ResponseEntity<>("Comment not found.", HttpStatus.NOT_FOUND);
        }
        Reaction reaction = reactionService.reactToComment(user, comment, type);
        if (reaction == null)
            return new ResponseEntity<>(reactionService.countsForComment(comment.getId()), HttpStatus.OK);

        return new ResponseEntity<>(new ReactionDTO(reaction), HttpStatus.OK);
    }

    @GetMapping("/mine/event/{eventId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> myReactionOnEvent(@PathVariable String eventId, @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Reaction reaction = reactionService.findReactionOnEvent(user.getId(), Long.parseLong(eventId));
        if (reaction == null)
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        return new ResponseEntity<>(new ReactionDTO(reaction), HttpStatus.OK);
    }

    @GetMapping("/mine/comment/{commentId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> myReactionOnComment(@PathVariable String commentId, @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Reaction reaction = reactionService.findReactionOnComment(user.getId(), Long.parseLong(commentId));
        if (reaction == null)
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);

        return new ResponseEntity<>(new ReactionDTO(reaction), HttpStatus.OK);
    }

    private ReactionType parseType(String value) {
        if (value == null)
            return null;
        try {
            return ReactionType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private User findUserByToken(String token) {
        String cleanToken = token.substring(7);
        String username = tokenUtils.getUsernameFromToken(cleanToken);
        return userService.findByUsername(username);
    }
}

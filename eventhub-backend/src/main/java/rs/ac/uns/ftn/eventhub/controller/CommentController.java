package rs.ac.uns.ftn.eventhub.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.eventhub.model.dto.CommentDTO;
import rs.ac.uns.ftn.eventhub.model.entity.Comment;
import rs.ac.uns.ftn.eventhub.model.entity.Event;
import rs.ac.uns.ftn.eventhub.model.entity.User;
import rs.ac.uns.ftn.eventhub.security.TokenUtils;
import rs.ac.uns.ftn.eventhub.service.CommentService;
import rs.ac.uns.ftn.eventhub.service.EventService;
import rs.ac.uns.ftn.eventhub.service.UserService;
import rs.ac.uns.ftn.eventhub.service.implementation.CommentServiceImpl;
import rs.ac.uns.ftn.eventhub.service.implementation.EventServiceImpl;
import rs.ac.uns.ftn.eventhub.service.implementation.UserServiceImpl;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("api/comments")
public class CommentController {

    // Komentar ne sme biti duzi od kolone u bazi
    private static final int MAX_LENGTH = 200;


    CommentService commentService;


    EventService eventService;


    UserService userService;


    TokenUtils tokenUtils;

    private static final Logger logger = LogManager.getLogger(CommentController.class);

    @Autowired
    public CommentController(CommentServiceImpl commentService, EventServiceImpl eventService,
                             UserServiceImpl userService, TokenUtils tokenUtils) {
        this.commentService = commentService;
        this.eventService = eventService;
        this.userService = userService;
        this.tokenUtils = tokenUtils;
    }

    // Komentari se citaju i bez prijave, jer su stranice dogadjaja javne

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<CommentDTO>> getCommentsForEvent(@PathVariable String eventId) {
        logger.info("Finding comments for event with id: " + eventId);
        logger.info("Created and sent response");

        return new ResponseEntity<>(toDTOs(commentService.findCommentsForEvent(Long.parseLong(eventId), "desc")), HttpStatus.OK);
    }

    @GetMapping("/event/{eventId}/sort/{order}")
    public ResponseEntity<List<CommentDTO>> getCommentsForEventSorted(@PathVariable String eventId, @PathVariable String order) {
        logger.info("Finding comments for event with id: " + eventId + " sorted " + order);
        logger.info("Created and sent response");

        return new ResponseEntity<>(toDTOs(commentService.findCommentsForEvent(Long.parseLong(eventId), order)), HttpStatus.OK);
    }

    @GetMapping("/{id}/replies")
    public ResponseEntity<List<CommentDTO>> getReplies(@PathVariable String id) {
        logger.info("Finding replies for comment with id: " + id);
        logger.info("Created and sent response");

        return new ResponseEntity<>(toDTOs(commentService.findRepliesForComment(Long.parseLong(id))), HttpStatus.OK);
    }

    // Od ove tacke rute traze prijavljenog korisnika

    @PostMapping("/event/{eventId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> addComment(@PathVariable String eventId, @RequestBody CommentDTO newComment,
                                        @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        ResponseEntity<String> textProblem = checkText(newComment.getText());
        if (textProblem != null)
            return textProblem;

        Event event = eventService.findById(Long.parseLong(eventId));
        if (event == null) {
            logger.error("Event not found with id: " + eventId);
            return new ResponseEntity<>("Event not found.", HttpStatus.NOT_FOUND);
        }
        logger.info("Creating comment of user with id: " + user.getId() + " on event with id: " + eventId);
        Comment comment = commentService.createComment(newComment.getText(), user, event, null);

        return new ResponseEntity<>(toDTO(comment), HttpStatus.CREATED);
    }

    @PostMapping("/{id}/reply")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> addReply(@PathVariable String id, @RequestBody CommentDTO newReply,
                                      @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        ResponseEntity<String> textProblem = checkText(newReply.getText());
        if (textProblem != null)
            return textProblem;

        Comment parent = commentService.findById(Long.parseLong(id));
        if (parent == null) {
            logger.error("Comment not found with id: " + id);
            return new ResponseEntity<>("Comment not found.", HttpStatus.NOT_FOUND);
        }
        Event event = eventService.findById(parent.getBelongsToEvent().getId());
        logger.info("Creating reply of user with id: " + user.getId() + " to comment with id: " + id);
        Comment reply = commentService.createComment(newReply.getText(), user, event, parent);

        return new ResponseEntity<>(toDTO(reply), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> editComment(@PathVariable String id, @RequestBody CommentDTO editedComment,
                                         @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Comment comment = commentService.findById(Long.parseLong(id));
        if (comment == null) {
            logger.error("Comment not found with id: " + id);
            return new ResponseEntity<>("Comment not found.", HttpStatus.NOT_FOUND);
        }
        // Svoj komentar menja samo autor, ni administrator ne pise u tudje ime
        if (!comment.getBelongsToUser().getId().equals(user.getId())) {
            logger.error("User with id: " + user.getId() + " is not the author of comment with id: " + id);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        ResponseEntity<String> textProblem = checkText(editedComment.getText());
        if (textProblem != null)
            return textProblem;

        logger.info("Applying changes of comment with id: " + id);
        comment.setText(editedComment.getText());
        comment = commentService.updateComment(comment);

        return new ResponseEntity<>(toDTO(comment), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> deleteComment(@PathVariable String id, @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Comment comment = commentService.findById(Long.parseLong(id));
        if (comment == null) {
            logger.error("Comment not found with id: " + id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // Brise autor, administrator, ili onaj ko vodi dogadjaj
        boolean isAuthor = comment.getBelongsToUser().getId().equals(user.getId());
        Event event = eventService.findById(comment.getBelongsToEvent().getId());
        boolean managesEvent = event != null && event.getCreatedBy().getId().equals(user.getId());
        if (!isAuthor && !managesEvent && !user.isAdmin()) {
            logger.error("User with id: " + user.getId() + " is not allowed to delete comment with id: " + id);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        logger.info("Deleting comment with id: " + id);
        commentService.deleteComment(comment.getId());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    private ResponseEntity<String> checkText(String text) {
        if (text == null || text.isBlank()) {
            logger.error("Comment text is empty");
            return new ResponseEntity<>("A comment cannot be empty.", HttpStatus.BAD_REQUEST);
        }
        if (text.length() > MAX_LENGTH) {
            logger.error("Comment text is longer than " + MAX_LENGTH + " characters");
            return new ResponseEntity<>("A comment can have at most " + MAX_LENGTH + " characters.", HttpStatus.BAD_REQUEST);
        }
        return null;
    }

    // Uz komentar se salje i ime autora, da front ne bi za svaki komentar trazio korisnika posebno
    private CommentDTO toDTO(Comment comment) {
        CommentDTO dto = new CommentDTO(comment);
        User author = userService.findById(comment.getBelongsToUser().getId());
        if (author != null)
            dto.setAuthorUsername(author.getUsername());
        return dto;
    }

    private List<CommentDTO> toDTOs(List<Comment> comments) {
        List<CommentDTO> dtos = new ArrayList<>();
        for (Comment temp : comments) {
            dtos.add(toDTO(temp));
        }
        return dtos;
    }

    private User findUserByToken(String token) {
        String cleanToken = token.substring(7);
        String username = tokenUtils.getUsernameFromToken(cleanToken);
        return userService.findByUsername(username);
    }
}

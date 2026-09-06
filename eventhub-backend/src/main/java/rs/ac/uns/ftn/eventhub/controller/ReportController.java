package rs.ac.uns.ftn.eventhub.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.eventhub.model.dto.ReportDTO;
import rs.ac.uns.ftn.eventhub.model.entity.Comment;
import rs.ac.uns.ftn.eventhub.model.entity.Event;
import rs.ac.uns.ftn.eventhub.model.entity.Report;
import rs.ac.uns.ftn.eventhub.model.entity.User;
import rs.ac.uns.ftn.eventhub.model.enums.ReportReason;
import rs.ac.uns.ftn.eventhub.security.TokenUtils;
import rs.ac.uns.ftn.eventhub.service.*;
import rs.ac.uns.ftn.eventhub.service.implementation.*;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("api/reports")
public class ReportController {


    ReportService reportService;


    EventService eventService;


    CommentService commentService;


    CommunityService communityService;


    UserService userService;


    TokenUtils tokenUtils;

    private static final Logger logger = LogManager.getLogger(ReportController.class);

    @Autowired
    public ReportController(ReportServiceImpl reportService, EventServiceImpl eventService,
                            CommentServiceImpl commentService, CommunityServiceImpl communityService,
                            UserServiceImpl userService, TokenUtils tokenUtils) {
        this.reportService = reportService;
        this.eventService = eventService;
        this.commentService = commentService;
        this.communityService = communityService;
        this.userService = userService;
        this.tokenUtils = tokenUtils;
    }

    @PostMapping()
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> createReport(@RequestBody ReportDTO newReport, @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        ReportReason reason = parseReason(newReport.getReason());
        if (reason == null) {
            logger.error("Unknown report reason: " + newReport.getReason());
            return new ResponseEntity<>("Unknown report reason.", HttpStatus.BAD_REQUEST);
        }
        // Prijava se odnosi na tacno jednu stvar: korisnika, dogadjaj ili komentar
        int targets = (newReport.getOnUserId() != null ? 1 : 0)
                + (newReport.getOnEventId() != null ? 1 : 0)
                + (newReport.getOnCommentId() != null ? 1 : 0);
        if (targets != 1) {
            logger.error("A report must have exactly one target");
            return new ResponseEntity<>("A report must be about exactly one user, event or comment.", HttpStatus.BAD_REQUEST);
        }

        User onUser = null;
        Event onEvent = null;
        Comment onComment = null;

        if (newReport.getOnUserId() != null) {
            onUser = userService.findById(newReport.getOnUserId());
            if (onUser == null)
                return new ResponseEntity<>("User not found.", HttpStatus.NOT_FOUND);
            if (onUser.getId().equals(user.getId())) {
                logger.error("User with id: " + user.getId() + " tried to report themselves");
                return new ResponseEntity<>("You cannot report yourself.", HttpStatus.BAD_REQUEST);
            }
        }
        if (newReport.getOnEventId() != null) {
            onEvent = eventService.findById(newReport.getOnEventId());
            if (onEvent == null)
                return new ResponseEntity<>("Event not found.", HttpStatus.NOT_FOUND);
        }
        if (newReport.getOnCommentId() != null) {
            onComment = commentService.findById(newReport.getOnCommentId());
            if (onComment == null)
                return new ResponseEntity<>("Comment not found.", HttpStatus.NOT_FOUND);
        }

        if (reportService.findPendingReportFromUser(user.getId(), newReport.getOnUserId(),
                newReport.getOnEventId(), newReport.getOnCommentId()) != null) {
            logger.error("User with id: " + user.getId() + " already has a pending report for this content");
            return new ResponseEntity<>("You have already reported this, it is still being reviewed.", HttpStatus.CONFLICT);
        }

        logger.info("Creating report from user with id: " + user.getId());
        Report report = reportService.createReport(reason, user, onUser, onEvent, onComment);

        return new ResponseEntity<>(new ReportDTO(report), HttpStatus.CREATED);
    }

    // Javan sadrzaj pregleda administrator sistema
    @GetMapping()
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<ReportDTO>> getAll(@RequestParam(value = "pending", required = false) Boolean pending,
                                                  @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        logger.info("Finding reports");

        return new ResponseEntity<>(toDTOs(reportService.findAll(Boolean.TRUE.equals(pending))), HttpStatus.OK);
    }

    // Sadrzaj unutar zajednice pregleda njen organizator
    @GetMapping("/community/{communityId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<ReportDTO>> getForCommunity(@PathVariable String communityId,
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
        logger.info("Finding reports for community with id: " + communityId);

        return new ResponseEntity<>(toDTOs(reportService.findReportsForCommunity(Long.parseLong(communityId))), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> getOne(@PathVariable String id, @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Report report = reportService.findById(Long.parseLong(id));
        if (report == null) {
            logger.error("Report not found with id: " + id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // Prijavu vidi samo onaj ko sme i da odluci o njoj
        if (!canReview(user, report)) {
            logger.error("User with id: " + user.getId() + " is not allowed to see report with id: " + id);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        return new ResponseEntity<>(new ReportDTO(report), HttpStatus.OK);
    }

    @PatchMapping("/{id}/accept")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> accept(@PathVariable String id, @RequestHeader("authorization") String token) {
        return decide(id, token, true);
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> reject(@PathVariable String id, @RequestHeader("authorization") String token) {
        return decide(id, token, false);
    }

    private ResponseEntity<?> decide(String id, String token, boolean accepted) {
        logger.info("Authentication check");
        User user = findUserByToken(token);
        if (user == null) {
            logger.error("User not found with token: " + token);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Report report = reportService.findById(Long.parseLong(id));
        if (report == null) {
            logger.error("Report not found with id: " + id);
            return new ResponseEntity<>("Report not found.", HttpStatus.NOT_FOUND);
        }
        if (report.getAccepted() != null) {
            logger.error("Report with id: " + id + " has already been reviewed");
            return new ResponseEntity<>("This report has already been reviewed.", HttpStatus.CONFLICT);
        }
        if (!canReview(user, report)) {
            logger.error("User with id: " + user.getId() + " is not allowed to review report with id: " + id);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        report = reportService.decide(report, accepted);

        // Prihvacena prijava znaci da se sadrzaj sklanja sa sistema
        if (accepted) {
            if (report.getOnComment() != null) {
                logger.info("Suspending comment with id: " + report.getOnComment().getId());
                commentService.deleteComment(report.getOnComment().getId());
            }
            if (report.getOnEvent() != null) {
                logger.info("Suspending event with id: " + report.getOnEvent().getId());
                eventService.deleteEvent(report.getOnEvent().getId());
            }
        }

        return new ResponseEntity<>(new ReportDTO(report), HttpStatus.OK);
    }

    // Prijavu na sadrzaj u zajednici resava njen organizator, sve ostalo administrator sistema
    private boolean canReview(User user, Report report) {
        if (user.isAdmin())
            return true;

        Long eventId = null;
        if (report.getOnEvent() != null)
            eventId = report.getOnEvent().getId();
        if (report.getOnComment() != null) {
            Comment comment = commentService.findById(report.getOnComment().getId());
            if (comment != null && comment.getBelongsToEvent() != null)
                eventId = comment.getBelongsToEvent().getId();
        }
        if (eventId == null)
            return false;

        Long communityId = eventService.findCommunityIdForEvent(eventId);
        return communityId != null && communityService.checkOrganizer(communityId, user.getId());
    }

    private ReportReason parseReason(String value) {
        if (value == null)
            return null;
        try {
            return ReportReason.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private List<ReportDTO> toDTOs(List<Report> reports) {
        List<ReportDTO> dtos = new ArrayList<>();
        for (Report temp : reports) {
            dtos.add(new ReportDTO(temp));
        }
        return dtos;
    }

    private User findUserByToken(String token) {
        String cleanToken = token.substring(7);
        String username = tokenUtils.getUsernameFromToken(cleanToken);
        return userService.findByUsername(username);
    }
}

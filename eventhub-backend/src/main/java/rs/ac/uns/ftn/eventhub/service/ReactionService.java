package rs.ac.uns.ftn.eventhub.service;

import rs.ac.uns.ftn.eventhub.model.entity.Comment;
import rs.ac.uns.ftn.eventhub.model.entity.Event;
import rs.ac.uns.ftn.eventhub.model.entity.Reaction;
import rs.ac.uns.ftn.eventhub.model.entity.User;
import rs.ac.uns.ftn.eventhub.model.enums.ReactionType;

import java.util.Map;

public interface ReactionService {

    Reaction findReactionOnEvent(Long userId, Long eventId);

    Reaction findReactionOnComment(Long userId, Long commentId);

    // Vraca reakciju, ili null ako je ista reakcija ponovljena pa je time uklonjena
    Reaction reactToEvent(User user, Event event, ReactionType type);

    Reaction reactToComment(User user, Comment comment, ReactionType type);

    Map<String, Integer> countsForEvent(Long eventId);

    Map<String, Integer> countsForComment(Long commentId);

    Integer deleteReactionsForEvent(Long eventId);

    Integer deleteReactionsForComment(Long commentId);
}

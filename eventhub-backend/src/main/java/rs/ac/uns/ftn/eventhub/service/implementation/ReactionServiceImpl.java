package rs.ac.uns.ftn.eventhub.service.implementation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.eventhub.model.entity.Comment;
import rs.ac.uns.ftn.eventhub.model.entity.Event;
import rs.ac.uns.ftn.eventhub.model.entity.Reaction;
import rs.ac.uns.ftn.eventhub.model.entity.User;
import rs.ac.uns.ftn.eventhub.model.enums.ReactionType;
import rs.ac.uns.ftn.eventhub.repository.ReactionRepository;
import rs.ac.uns.ftn.eventhub.service.ReactionService;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class ReactionServiceImpl implements ReactionService {


    private ReactionRepository reactionRepository;


    @Autowired
    public ReactionServiceImpl(ReactionRepository reactionRepository) {
        this.reactionRepository = reactionRepository;
    }

    private static final Logger logger = LogManager.getLogger(ReactionServiceImpl.class);

    @Override
    public Reaction findReactionOnEvent(Long userId, Long eventId) {
        return reactionRepository.findReactionOnEvent(userId, eventId).orElse(null);
    }

    @Override
    public Reaction findReactionOnComment(Long userId, Long commentId) {
        return reactionRepository.findReactionOnComment(userId, commentId).orElse(null);
    }

    // Ista reakcija po drugi put se povlaci, druga reakcija menja postojecu.
    // Time korisnik uvek ima najvise jednu reakciju po sadrzaju.
    @Override
    public Reaction reactToEvent(User user, Event event, ReactionType type) {
        Reaction existing = findReactionOnEvent(user.getId(), event.getId());

        if (existing != null) {
            if (existing.getType() == type) {
                logger.info("Removing reaction with id: " + existing.getId());
                reactionRepository.deleteReactionById(existing.getId());
                return null;
            }
            logger.info("Changing reaction with id: " + existing.getId() + " to " + type);
            existing.setType(type);
            existing.setTimestamp(LocalDate.now());
            return reactionRepository.save(existing);
        }

        Reaction newReaction = new Reaction();
        newReaction.setType(type);
        newReaction.setTimestamp(LocalDate.now());
        newReaction.setMadeBy(user);
        newReaction.setOnEvent(event);
        newReaction.setDeleted(false);

        return reactionRepository.save(newReaction);
    }

    @Override
    public Reaction reactToComment(User user, Comment comment, ReactionType type) {
        Reaction existing = findReactionOnComment(user.getId(), comment.getId());

        if (existing != null) {
            if (existing.getType() == type) {
                logger.info("Removing reaction with id: " + existing.getId());
                reactionRepository.deleteReactionById(existing.getId());
                return null;
            }
            logger.info("Changing reaction with id: " + existing.getId() + " to " + type);
            existing.setType(type);
            existing.setTimestamp(LocalDate.now());
            return reactionRepository.save(existing);
        }

        Reaction newReaction = new Reaction();
        newReaction.setType(type);
        newReaction.setTimestamp(LocalDate.now());
        newReaction.setMadeBy(user);
        newReaction.setOnComment(comment);
        newReaction.setDeleted(false);

        return reactionRepository.save(newReaction);
    }

    @Override
    public Map<String, Integer> countsForEvent(Long eventId) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (ReactionType type : ReactionType.values()) {
            counts.put(type.name(), reactionRepository.countForEvent(eventId, type.name()));
        }
        return counts;
    }

    @Override
    public Map<String, Integer> countsForComment(Long commentId) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (ReactionType type : ReactionType.values()) {
            counts.put(type.name(), reactionRepository.countForComment(commentId, type.name()));
        }
        return counts;
    }

    @Override
    public Integer deleteReactionsForEvent(Long eventId) {
        return reactionRepository.deleteReactionsForEvent(eventId);
    }

    @Override
    public Integer deleteReactionsForComment(Long commentId) {
        return reactionRepository.deleteReactionsForComment(commentId);
    }
}

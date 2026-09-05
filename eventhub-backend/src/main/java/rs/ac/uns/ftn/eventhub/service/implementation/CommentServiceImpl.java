package rs.ac.uns.ftn.eventhub.service.implementation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.eventhub.model.entity.Comment;
import rs.ac.uns.ftn.eventhub.model.entity.Event;
import rs.ac.uns.ftn.eventhub.model.entity.User;
import rs.ac.uns.ftn.eventhub.repository.CommentRepository;
import rs.ac.uns.ftn.eventhub.service.CommentService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CommentServiceImpl implements CommentService {


    private CommentRepository commentRepository;


    @Autowired
    public CommentServiceImpl(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    private static final Logger logger = LogManager.getLogger(CommentServiceImpl.class);

    @Override
    public Comment findById(Long id) {
        Optional<Comment> comment = commentRepository.findById(id);
        if (!comment.isEmpty())
            return comment.get();
        logger.error("Repository search for comment with id: " + id + " returned null");
        return null;
    }

    @Override
    public List<Comment> findCommentsForEvent(Long eventId, String order) {
        if ("asc".equals(order))
            return commentRepository.findCommentsForEventAsc(eventId).orElse(Collections.emptyList());
        return commentRepository.findCommentsForEvent(eventId).orElse(Collections.emptyList());
    }

    @Override
    public List<Comment> findRepliesForComment(Long commentId) {
        return commentRepository.findRepliesForComment(commentId).orElse(Collections.emptyList());
    }

    @Override
    public Comment createComment(String text, User author, Event event, Comment repliesTo) {
        Comment newComment = new Comment();
        newComment.setText(text);
        newComment.setTimestamp(LocalDate.now());
        newComment.setBelongsToUser(author);
        newComment.setBelongsToEvent(event);
        newComment.setRepliesTo(repliesTo);
        newComment.setDeleted(false);

        return commentRepository.save(newComment);
    }

    @Override
    public Comment updateComment(Comment comment) {
        return commentRepository.save(comment);
    }

    // Odgovor moze da ima svoj odgovor, pa se brisanje spusta kroz ceo lanac.
    // Bez toga bi odgovori na obrisan odgovor ostali da vise u bazi.
    @Override
    public Integer deleteComment(Long id) {
        for (Comment reply : findRepliesForComment(id)) {
            deleteComment(reply.getId());
        }
        return commentRepository.deleteCommentById(id);
    }

    @Override
    public Integer deleteCommentsForEvent(Long eventId) {
        return commentRepository.deleteCommentsForEvent(eventId);
    }
}

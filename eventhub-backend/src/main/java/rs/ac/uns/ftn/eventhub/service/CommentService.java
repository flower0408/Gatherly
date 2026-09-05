package rs.ac.uns.ftn.eventhub.service;

import rs.ac.uns.ftn.eventhub.model.entity.Comment;
import rs.ac.uns.ftn.eventhub.model.entity.Event;
import rs.ac.uns.ftn.eventhub.model.entity.User;

import java.util.List;

public interface CommentService {

    Comment findById(Long id);

    List<Comment> findCommentsForEvent(Long eventId, String sortBy, String order);

    List<Comment> findRepliesForComment(Long commentId);

    Comment createComment(String text, User author, Event event, Comment repliesTo);

    Comment updateComment(Comment comment);

    Integer deleteComment(Long id);

    Integer deleteCommentsForEvent(Long eventId);
}

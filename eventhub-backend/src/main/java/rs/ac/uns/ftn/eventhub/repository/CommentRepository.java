package rs.ac.uns.ftn.eventhub.repository;

import rs.ac.uns.ftn.eventhub.model.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Komentari prvog nivoa, odgovori se dohvataju posebno
    @Query(nativeQuery = true,
            value = "select c.* from comment c join `user` u on c.belongs_to_user_id = u.id " +
                    "where c.belongs_to_event_id = :eventId and c.replies_to_comment_id is null " +
                    "and c.is_deleted = false and u.is_deleted = false " +
                    "order by c.timestamp desc")
    Optional<List<Comment>> findCommentsForEvent(@Param("eventId") Long eventId);

    @Query(nativeQuery = true,
            value = "select c.* from comment c join `user` u on c.belongs_to_user_id = u.id " +
                    "where c.belongs_to_event_id = :eventId and c.replies_to_comment_id is null " +
                    "and c.is_deleted = false and u.is_deleted = false " +
                    "order by c.timestamp asc")
    Optional<List<Comment>> findCommentsForEventAsc(@Param("eventId") Long eventId);

    // Sortiranje po broju reakcija zadatog tipa. Smer se ne moze proslediti kao parametar,
    // pa postoje dva upita, a tip reakcije jeste parametar.
    @Query(nativeQuery = true,
            value = "select c.* from comment c join `user` u on c.belongs_to_user_id = u.id " +
                    "where c.belongs_to_event_id = :eventId and c.replies_to_comment_id is null " +
                    "and c.is_deleted = false and u.is_deleted = false " +
                    "order by (select count(*) from reaction r where r.on_comment_id = c.id " +
                    "          and r.type = :type and r.is_deleted = false) desc, c.timestamp desc")
    Optional<List<Comment>> findCommentsForEventByReactionDesc(@Param("eventId") Long eventId, @Param("type") String type);

    @Query(nativeQuery = true,
            value = "select c.* from comment c join `user` u on c.belongs_to_user_id = u.id " +
                    "where c.belongs_to_event_id = :eventId and c.replies_to_comment_id is null " +
                    "and c.is_deleted = false and u.is_deleted = false " +
                    "order by (select count(*) from reaction r where r.on_comment_id = c.id " +
                    "          and r.type = :type and r.is_deleted = false) asc, c.timestamp desc")
    Optional<List<Comment>> findCommentsForEventByReactionAsc(@Param("eventId") Long eventId, @Param("type") String type);

    @Query(nativeQuery = true,
            value = "select c.* from comment c join `user` u on c.belongs_to_user_id = u.id " +
                    "where c.replies_to_comment_id = :commentId " +
                    "and c.is_deleted = false and u.is_deleted = false " +
                    "order by c.timestamp asc")
    Optional<List<Comment>> findRepliesForComment(@Param("commentId") Long commentId);

    @Transactional
    Integer deleteCommentById(Long id);

    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = "update comment set is_deleted = true where belongs_to_event_id = :eventId")
    Integer deleteCommentsForEvent(@Param("eventId") Long eventId);
}

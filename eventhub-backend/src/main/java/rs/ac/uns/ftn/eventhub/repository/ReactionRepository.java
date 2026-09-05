package rs.ac.uns.ftn.eventhub.repository;

import rs.ac.uns.ftn.eventhub.model.entity.Reaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {

    // Jedan korisnik ima najvise jednu reakciju po dogadjaju
    @Query(nativeQuery = true,
            value = "select * from reaction where made_by_user_id = :userId and on_event_id = :eventId " +
                    "and is_deleted = false limit 1")
    Optional<Reaction> findReactionOnEvent(@Param("userId") Long userId, @Param("eventId") Long eventId);

    @Query(nativeQuery = true,
            value = "select * from reaction where made_by_user_id = :userId and on_comment_id = :commentId " +
                    "and is_deleted = false limit 1")
    Optional<Reaction> findReactionOnComment(@Param("userId") Long userId, @Param("commentId") Long commentId);

    @Query(nativeQuery = true,
            value = "select count(*) from reaction where on_event_id = :eventId and type = :type and is_deleted = false")
    Integer countForEvent(@Param("eventId") Long eventId, @Param("type") String type);

    @Query(nativeQuery = true,
            value = "select count(*) from reaction where on_comment_id = :commentId and type = :type and is_deleted = false")
    Integer countForComment(@Param("commentId") Long commentId, @Param("type") String type);

    @Transactional
    Integer deleteReactionById(Long id);

    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = "update reaction set is_deleted = true where on_event_id = :eventId")
    Integer deleteReactionsForEvent(@Param("eventId") Long eventId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = "update reaction set is_deleted = true where on_comment_id = :commentId")
    Integer deleteReactionsForComment(@Param("commentId") Long commentId);
}

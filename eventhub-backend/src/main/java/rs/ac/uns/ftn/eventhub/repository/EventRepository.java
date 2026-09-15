package rs.ac.uns.ftn.eventhub.repository;

import rs.ac.uns.ftn.eventhub.model.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query(nativeQuery = true,
            value = "select * from `event` where is_deleted = false order by starts_at asc;")
    Optional<List<Event>> findAllActiveEvents();

    @Query(nativeQuery = true,
            value = "select * from `event` where is_deleted = false and starts_at >= now() order by starts_at asc;")
    Optional<List<Event>> findUpcomingEvents();

    // Pretraga po pojmu, kategoriji i rasponu datuma. Prazan uslov se prosledjuje kao null
    // i tada se taj deo upita ne primenjuje, pa jedan upit pokriva sve kombinacije filtera.
    @Query(nativeQuery = true,
            value = "select * from `event` where is_deleted = false " +
                    "and (:term is null or lower(title) like lower(concat('%', :term, '%')) " +
                    "     or lower(description) like lower(concat('%', :term, '%')) " +
                    "     or lower(location) like lower(concat('%', :term, '%'))) " +
                    "and (:category is null or category = :category) " +
                    "and (:fromDate is null or starts_at >= :fromDate) " +
                    "and (:toDate is null or starts_at <= :toDate) " +
                    "and (:onlyUpcoming = false or starts_at >= now()) " +
                    "order by starts_at asc")
    Optional<List<Event>> searchEvents(@Param("term") String term,
                                       @Param("category") String category,
                                       @Param("fromDate") String fromDate,
                                       @Param("toDate") String toDate,
                                       @Param("onlyUpcoming") boolean onlyUpcoming);

    @Query(nativeQuery = true,
            value = "select * from `event` where created_by_user_id = :userId and is_deleted = false order by starts_at asc;")
    Optional<List<Event>> findEventsByCreator(@Param("userId") Long userId);

    @Query(nativeQuery = true,
            value = "select e.* from `event` e " +
                    "join `user` u on e.created_by_user_id = u.id " +
                    "where e.id in (select event_id from community_events where community_id = :communityId) " +
                    "and e.is_deleted = false and u.is_deleted = false;")
    Optional<List<Event>> findEventsByCommunityId(@Param("communityId") Long communityId);

    @Query(nativeQuery = true,
            value = "select e.* from `event` e " +
                    "join `user` u on e.created_by_user_id = u.id " +
                    "where e.id in (select event_id from community_events where community_id = :communityId) " +
                    "and e.is_deleted = false and u.is_deleted = false " +
                    "order by e.starts_at asc;")
    Optional<List<Event>> findEventsByCommunityIdAsc(@Param("communityId") Long communityId);

    @Query(nativeQuery = true,
            value = "select e.* from `event` e " +
                    "join `user` u on e.created_by_user_id = u.id " +
                    "where e.id in (select event_id from community_events where community_id = :communityId) " +
                    "and e.is_deleted = false and u.is_deleted = false " +
                    "order by e.starts_at desc;")
    Optional<List<Event>> findEventsByCommunityIdDesc(@Param("communityId") Long communityId);

    // Trazi dogadjaj koji se vremenski preklapa sa zadatim, a na koji je korisnik vec prijavljen.
    // Dva intervala se preklapaju ako svaki pocinje pre nego sto se onaj drugi zavrsi.
    @Query(nativeQuery = true,
            value = "select e.title from `event` e " +
                    "join event_registration r on r.for_event_id = e.id " +
                    "where r.created_by_user_id = :userId and r.is_deleted = false " +
                    "and r.status in ('PENDING', 'ACCEPTED', 'WAITLISTED') " +
                    "and e.is_deleted = false and e.id <> :eventId " +
                    "and e.starts_at < :endsAt and e.ends_at > :startsAt " +
                    "limit 1")
    Optional<String> findConflictingEventTitle(@Param("userId") Long userId, @Param("eventId") Long eventId,
                                               @Param("startsAt") LocalDateTime startsAt, @Param("endsAt") LocalDateTime endsAt);

    // Vraca se samo id zajednice, jer Spring Data nativan upit ne ume da mapira u pojedinacan entitet
    @Query(nativeQuery = true,
            value = "select community_id from community_events where event_id = :eventId")
    Optional<Long> findCommunityIdByEventId(@Param("eventId") Long eventId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = "insert into community_events (community_id, event_id) values (:communityId, :eventId)")
    Integer saveCommunityEvent(@Param("communityId") Long communityId, @Param("eventId") Long eventId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = "delete from community_events where event_id = :id")
    Integer deleteEventFromCommunity(@Param("id") Long id);

    @Transactional
    Integer deleteEventById(Long id);
}

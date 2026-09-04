package rs.ac.uns.ftn.eventhub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.eventhub.model.entity.EventRegistration;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {

    @Query(nativeQuery = true,
            value = "select * from event_registration where for_event_id = :eventId and is_deleted = false " +
                    "order by created_at asc;")
    Optional<List<EventRegistration>> findRegistrationsByEventId(@Param("eventId") Long eventId);

    @Query(nativeQuery = true,
            value = "select * from event_registration where created_by_user_id = :userId and is_deleted = false " +
                    "order by created_at desc;")
    Optional<List<EventRegistration>> findRegistrationsByUserId(@Param("userId") Long userId);

    // Prijava koja je jos uvek aktivna, tj. nije otkazana ni odbijena
    @Query(nativeQuery = true,
            value = "select * from event_registration where created_by_user_id = :userId and for_event_id = :eventId " +
                    "and is_deleted = false and status not in ('CANCELLED', 'REJECTED') limit 1;")
    Optional<EventRegistration> findActiveRegistration(@Param("userId") Long userId, @Param("eventId") Long eventId);

    @Query(nativeQuery = true,
            value = "select count(*) from event_registration where for_event_id = :eventId and is_deleted = false " +
                    "and status in ('ACCEPTED', 'ATTENDED', 'NO_SHOW');")
    Integer countTakenSpots(@Param("eventId") Long eventId);

    // Osnova za skor pouzdanosti: koliko je puta korisnik dosao, a koliko izostao
    @Query(nativeQuery = true,
            value = "select count(*) from event_registration where created_by_user_id = :userId " +
                    "and is_deleted = false and status = :status")
    Integer countByUserAndStatus(@Param("userId") Long userId, @Param("status") String status);

    // Prvi sa liste cekanja je onaj ko se najranije prijavio
    @Query(nativeQuery = true,
            value = "select * from event_registration where for_event_id = :eventId and is_deleted = false " +
                    "and status = 'WAITLISTED' order by created_at asc limit 1;")
    Optional<EventRegistration> findFirstWaitlisted(@Param("eventId") Long eventId);

    // Kada se dogadjaj obrise, njegove prijave prestaju da vaze
    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = "update event_registration set is_deleted = true where for_event_id = :eventId")
    Integer deleteRegistrationsForEvent(@Param("eventId") Long eventId);
}

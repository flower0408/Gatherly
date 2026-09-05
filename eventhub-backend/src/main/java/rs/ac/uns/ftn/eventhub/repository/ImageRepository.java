package rs.ac.uns.ftn.eventhub.repository;

import rs.ac.uns.ftn.eventhub.model.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {

    @Query(nativeQuery = true,
            value = "select * from image where belongs_to_event_id = :eventId and is_deleted = false")
    Optional<List<Image>> findImagesForEvent(@Param("eventId") Long eventId);

    @Query(nativeQuery = true,
            value = "select * from image where belongs_to_user_id = :userId and is_deleted = false limit 1")
    Optional<Image> findProfileImageForUser(@Param("userId") Long userId);

    @Transactional
    Integer deleteImageById(Long id);

    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = "update image set is_deleted = true where belongs_to_event_id = :eventId")
    Integer deleteImagesForEvent(@Param("eventId") Long eventId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = "update image set is_deleted = true where belongs_to_user_id = :userId")
    Integer deleteProfileImagesForUser(@Param("userId") Long userId);
}

package rs.ac.uns.ftn.eventhub.repository;

import rs.ac.uns.ftn.eventhub.model.entity.Banned;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface BannedRepository extends JpaRepository<Banned, Long> {

    // Blokada na nivou sistema nema zajednicu
    @Query(nativeQuery = true,
            value = "select * from banned where towards_user_id = :userId and for_community_id is null " +
                    "and is_deleted = false limit 1")
    Optional<Banned> findSystemBan(@Param("userId") Long userId);

    @Query(nativeQuery = true,
            value = "select * from banned where towards_user_id = :userId and for_community_id = :communityId " +
                    "and is_deleted = false limit 1")
    Optional<Banned> findCommunityBan(@Param("userId") Long userId, @Param("communityId") Long communityId);

    @Query(nativeQuery = true,
            value = "select * from banned where for_community_id is null and is_deleted = false order by timestamp desc")
    Optional<List<Banned>> findAllSystemBans();

    @Query(nativeQuery = true,
            value = "select * from banned where for_community_id = :communityId and is_deleted = false " +
                    "order by timestamp desc")
    Optional<List<Banned>> findBansForCommunity(@Param("communityId") Long communityId);

    // Odblokiranje se belezi kao gasenje zapisa o blokadi, pa istorija ostaje
    @Transactional
    Integer deleteBannedById(Long id);
}

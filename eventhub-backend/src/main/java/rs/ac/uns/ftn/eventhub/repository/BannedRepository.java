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

    // Filter uz svaki zahtev ima samo korisnicko ime, a ne i id
    @Query(nativeQuery = true,
            value = "select count(*) from banned b join `user` u on u.id = b.towards_user_id " +
                    "where u.username = :username and b.for_community_id is null and b.is_deleted = false")
    Integer countSystemBansForUsername(@Param("username") String username);

    @Query(nativeQuery = true,
            value = "select * from banned where for_community_id is null and is_deleted = false order by timestamp desc")
    Optional<List<Banned>> findAllSystemBans();

    // Administrator treba da vidi celu sliku, i blokade sa sistema i one po zajednicama
    @Query(nativeQuery = true,
            value = "select * from banned where is_deleted = false order by timestamp desc")
    Optional<List<Banned>> findAllBans();

    @Query(nativeQuery = true,
            value = "select * from banned where for_community_id = :communityId and is_deleted = false " +
                    "order by timestamp desc")
    Optional<List<Banned>> findBansForCommunity(@Param("communityId") Long communityId);

    // Odblokiranje se belezi kao gasenje zapisa o blokadi, pa istorija ostaje
    @Transactional
    Integer deleteBannedById(Long id);
}

package rs.ac.uns.ftn.eventhub.repository;

import rs.ac.uns.ftn.eventhub.model.entity.Community;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Long> {

    Optional<Community> findByName(String name);

    @Transactional
    Integer deleteCommunityById(Long id);

    @Query(nativeQuery = true,
            value = "select * from community where is_deleted = false;")
    Optional<List<Community>> findAllActiveCommunities();

    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = "insert into community_organizers (community_id, organizer_id) values (:communityId, :organizerId);")
    Integer addCommunityOrganizer(@Param("communityId") Long communityId, @Param("organizerId") Long organizerId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = "insert into community_members (community_id, member_id) values (:communityId, :memberId);")
    Integer addCommunityMember(@Param("communityId") Long communityId, @Param("memberId") Long memberId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = "delete from community_organizers where community_id = :communityId and organizer_id = :organizerId")
    Integer deleteCommunityOrganizer(@Param("communityId") Long communityId, @Param("organizerId") Long organizerId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = "delete from community_members where community_id = :communityId and member_id = :memberId")
    Integer deleteCommunityMember(@Param("communityId") Long communityId, @Param("memberId") Long memberId);

    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = "delete from community_organizers where community_id = :id")
    Integer deleteCommunityOrganizers(@Param("id") Long id);

    @Transactional
    @Modifying
    @Query(nativeQuery = true,
            value = "delete from community_members where community_id = :id")
    Integer deleteCommunityMembers(@Param("id") Long id);

    @Query(nativeQuery = true,
            value = "select * from community where id in (select community_id from community_members where member_id = :memberId)")
    Optional<List<Community>> findCommunitiesByMemberId(@Param("memberId") Long memberId);

    @Query(nativeQuery = true,
            value = "select count(*) from community_members where community_id = :communityId and member_id = :userId")
    Integer findUserInCommunity(@Param("communityId") Long communityId, @Param("userId") Long userId);

    @Query(nativeQuery = true,
            value = "select count(*) from community_organizers where community_id = :communityId and organizer_id = :userId")
    Integer findOrganizerInCommunity(@Param("communityId") Long communityId, @Param("userId") Long userId);

    @Query(nativeQuery = true,
            value = "select distinct member_id from community_members where community_id = :communityId")
    Optional<List<Long>> findCommunityMembers(@Param("communityId") Long communityId);

    @Query(nativeQuery = true,
            value = "select distinct organizer_id from community_organizers where community_id = :communityId")
    Optional<List<Long>> findCommunityOrganizers(@Param("communityId") Long communityId);

}

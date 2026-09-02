package rs.ac.uns.ftn.eventhub.service;

import rs.ac.uns.ftn.eventhub.model.dto.CommunityDTO;
import rs.ac.uns.ftn.eventhub.model.entity.Community;

import java.util.List;

public interface CommunityService {

    Community findById(Long id);

    Community findByName(String name);

    List<Community> findAll();

    List<Community> findCommunitiesForUser(Long userId);

    Community createCommunity(CommunityDTO communityDTO);

    Community updateCommunity(Community community);

    Integer deleteCommunity(Long id);

    Boolean addCommunityOrganizer(Long communityId, Long organizerId);

    Boolean addCommunityMember(Long communityId, Long memberId);

    Integer deleteCommunityOrganizer(Long communityId, Long organizerId);

    Integer deleteCommunityMember(Long communityId, Long memberId);

    Integer deleteCommunityOrganizers(Long id);

    Integer deleteCommunityMembers(Long id);

    Boolean checkMember(Long communityId, Long userId);

    Boolean checkOrganizer(Long communityId, Long userId);

    List<Long> findMembersByCommunityId(Long id);

    List<Long> findOrganizersByCommunityId(Long id);
}

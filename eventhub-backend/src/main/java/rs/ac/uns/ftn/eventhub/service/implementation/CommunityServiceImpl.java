package rs.ac.uns.ftn.eventhub.service.implementation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.eventhub.model.dto.CommunityDTO;
import rs.ac.uns.ftn.eventhub.model.entity.Community;
import rs.ac.uns.ftn.eventhub.repository.CommunityRepository;
import rs.ac.uns.ftn.eventhub.service.CommunityService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CommunityServiceImpl implements CommunityService {


    private CommunityRepository communityRepository;


    @Autowired
    public CommunityServiceImpl(CommunityRepository communityRepository) {
        this.communityRepository = communityRepository;
    }

    private static final Logger logger = LogManager.getLogger(CommunityServiceImpl.class);

    @Override
    public Community findById(Long id) {
        Optional<Community> community = communityRepository.findById(id);
        if (!community.isEmpty())
            return community.get();
        logger.error("Repository search for community with id: " + id + " returned null");
        return null;
    }

    @Override
    public Community findByName(String name) {
        Optional<Community> community = communityRepository.findByName(name);
        if (!community.isEmpty())
            return community.get();
        logger.error("Repository search for community with name: " + name + " returned null");
        return null;
    }

    @Override
    public List<Community> findAll() {
        return this.communityRepository.findAllActiveCommunities().orElse(Collections.emptyList());
    }

    @Override
    public List<Community> findCommunitiesForUser(Long userId) {
        return this.communityRepository.findCommunitiesByMemberId(userId).orElse(Collections.emptyList());
    }

    @Override
    public Community createCommunity(CommunityDTO communityDTO) {
        Optional<Community> community = communityRepository.findByName(communityDTO.getName());

        if (community.isPresent()) {
            logger.error("Community with name: " + communityDTO.getName() + " already exists in repository");
            return null;
        }

        Community newCommunity = new Community();
        newCommunity.setName(communityDTO.getName());
        newCommunity.setDescription(communityDTO.getDescription());
        newCommunity.setCreationDate(LocalDateTime.parse(communityDTO.getCreationDate()));
        newCommunity.setSuspended(communityDTO.isSuspended());
        newCommunity.setSuspendedReason(communityDTO.getSuspendedReason());
        newCommunity.setDeleted(false);
        newCommunity = communityRepository.save(newCommunity);

        return newCommunity;
    }

    @Override
    public Community updateCommunity(Community community) {
        return communityRepository.save(community);
    }

    @Override
    public Integer deleteCommunity(Long id) {
        return communityRepository.deleteCommunityById(id);
    }

    @Override
    public Boolean addCommunityOrganizer(Long communityId, Long organizerId) {
        return communityRepository.addCommunityOrganizer(communityId, organizerId) > 0;
    }

    @Override
    public Boolean addCommunityMember(Long communityId, Long memberId) {
        return communityRepository.addCommunityMember(communityId, memberId) > 0;
    }

    @Override
    public Integer deleteCommunityOrganizer(Long communityId, Long organizerId) {
        return communityRepository.deleteCommunityOrganizer(communityId, organizerId);
    }

    @Override
    public Integer deleteCommunityMember(Long communityId, Long memberId) {
        return communityRepository.deleteCommunityMember(communityId, memberId);
    }

    @Override
    public Integer deleteCommunityOrganizers(Long id) {
        return communityRepository.deleteCommunityOrganizers(id);
    }

    @Override
    public Integer deleteCommunityMembers(Long id) {
        return communityRepository.deleteCommunityMembers(id);
    }

    @Override
    public Boolean checkMember(Long communityId, Long userId) {
        return communityRepository.findUserInCommunity(communityId, userId) > 0;
    }

    @Override
    public Boolean checkOrganizer(Long communityId, Long userId) {
        return communityRepository.findOrganizerInCommunity(communityId, userId) > 0;
    }

    @Override
    public List<Long> findMembersByCommunityId(Long id) {
        return this.communityRepository.findCommunityMembers(id).orElse(Collections.emptyList());
    }

    @Override
    public List<Long> findOrganizersByCommunityId(Long id) {
        return this.communityRepository.findCommunityOrganizers(id).orElse(Collections.emptyList());
    }
}

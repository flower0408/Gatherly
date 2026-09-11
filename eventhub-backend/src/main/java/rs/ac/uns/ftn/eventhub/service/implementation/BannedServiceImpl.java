package rs.ac.uns.ftn.eventhub.service.implementation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.eventhub.model.entity.Banned;
import rs.ac.uns.ftn.eventhub.model.entity.Community;
import rs.ac.uns.ftn.eventhub.model.entity.User;
import rs.ac.uns.ftn.eventhub.repository.BannedRepository;
import rs.ac.uns.ftn.eventhub.service.BannedService;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@Service
public class BannedServiceImpl implements BannedService {


    private BannedRepository bannedRepository;


    @Autowired
    public BannedServiceImpl(BannedRepository bannedRepository) {
        this.bannedRepository = bannedRepository;
    }

    private static final Logger logger = LogManager.getLogger(BannedServiceImpl.class);

    @Override
    public Banned findById(Long id) {
        return bannedRepository.findById(id).orElse(null);
    }

    @Override
    public Banned findSystemBan(Long userId) {
        return bannedRepository.findSystemBan(userId).orElse(null);
    }

    @Override
    public Banned findCommunityBan(Long userId, Long communityId) {
        return bannedRepository.findCommunityBan(userId, communityId).orElse(null);
    }

    @Override
    public boolean isBannedFromSystem(Long userId) {
        return findSystemBan(userId) != null;
    }

    @Override
    public boolean isBannedFromSystem(String username) {
        Integer count = bannedRepository.countSystemBansForUsername(username);
        return count != null && count > 0;
    }

    @Override
    public boolean isBannedFromCommunity(Long userId, Long communityId) {
        return findCommunityBan(userId, communityId) != null;
    }

    @Override
    public List<Banned> findAllSystemBans() {
        return bannedRepository.findAllSystemBans().orElse(Collections.emptyList());
    }

    @Override
    public List<Banned> findAllBans() {
        return bannedRepository.findAllBans().orElse(Collections.emptyList());
    }

    @Override
    public List<Banned> findBansForCommunity(Long communityId) {
        return bannedRepository.findBansForCommunity(communityId).orElse(Collections.emptyList());
    }

    @Override
    public Banned ban(User bannedBy, User towardsUser, Community community) {
        logger.info("Banning user with id: " + towardsUser.getId()
                + (community == null ? " from the whole system" : " from community with id: " + community.getId()));

        Banned banned = new Banned();
        banned.setTimestamp(LocalDate.now());
        banned.setBannedBy(bannedBy);
        banned.setTowardsUser(towardsUser);
        banned.setCommunity(community);
        banned.setDeleted(false);

        return bannedRepository.save(banned);
    }

    @Override
    public Integer unban(Long id) {
        logger.info("Removing ban with id: " + id);
        return bannedRepository.deleteBannedById(id);
    }
}

package rs.ac.uns.ftn.eventhub.service;

import rs.ac.uns.ftn.eventhub.model.entity.Banned;
import rs.ac.uns.ftn.eventhub.model.entity.Community;
import rs.ac.uns.ftn.eventhub.model.entity.User;

import java.util.List;

public interface BannedService {

    Banned findById(Long id);

    Banned findSystemBan(Long userId);

    Banned findCommunityBan(Long userId, Long communityId);

    boolean isBannedFromSystem(Long userId);

    boolean isBannedFromCommunity(Long userId, Long communityId);

    List<Banned> findAllSystemBans();

    List<Banned> findBansForCommunity(Long communityId);

    Banned ban(User bannedBy, User towardsUser, Community community);

    Integer unban(Long id);
}

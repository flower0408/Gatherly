package rs.ac.uns.ftn.eventhub.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.ftn.eventhub.model.entity.Banned;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BannedDTO {

    private Long id;

    private String timestamp;

    private Long bannedByUserId;

    private Long towardsUserId;

    // Prazno znaci blokada na nivou celog sistema
    private Long forCommunityId;

    private String towardsUsername;

    public BannedDTO(Banned banned) {
        this.id = banned.getId();
        this.timestamp = banned.getTimestamp().toString();
        if (banned.getBannedBy() != null)
            this.bannedByUserId = banned.getBannedBy().getId();
        if (banned.getTowardsUser() != null)
            this.towardsUserId = banned.getTowardsUser().getId();
        if (banned.getCommunity() != null)
            this.forCommunityId = banned.getCommunity().getId();
    }
}

package rs.ac.uns.ftn.eventhub.model.dto;

import rs.ac.uns.ftn.eventhub.model.entity.Community;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommunityDTO {

    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotNull
    private String creationDate;

    @NotNull
    private boolean suspended;

    private String suspendedReason;

    public CommunityDTO(Community createdCommunity) {
        this.id = createdCommunity.getId();
        this.name = createdCommunity.getName();
        this.description = createdCommunity.getDescription();
        this.creationDate = createdCommunity.getCreationDate().toString();
        this.suspended = createdCommunity.isSuspended();
        this.suspendedReason = createdCommunity.getSuspendedReason();
    }
}

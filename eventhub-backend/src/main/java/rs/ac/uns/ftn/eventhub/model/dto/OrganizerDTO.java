package rs.ac.uns.ftn.eventhub.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.ftn.eventhub.model.entity.Organizer;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrganizerDTO {

    private Long id;

    private Long userId;

    private Long communityId;

    public OrganizerDTO(Organizer organizer){
        this.id = organizer.getId();
        this.userId = organizer.getUser().getId();
        this.communityId = organizer.getCommunity().getId();
    }


}

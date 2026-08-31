package rs.ac.uns.ftn.eventhub.model.dto;

import rs.ac.uns.ftn.eventhub.model.entity.Reaction;
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
public class ReactionDTO {

    private Long id = -1L;

    @NotBlank
    private String reactionType;

    @NotBlank
    private String timestamp;

    @NotNull
    private Long madeByUserId;

    private Long onCommentId;

    private Long onEventId;

    public ReactionDTO(Reaction reaction) {
        this.id = reaction.getId();
        this.reactionType = reaction.getType().toString();
        this.timestamp = reaction.getTimestamp().toString();
        this.madeByUserId = reaction.getMadeBy().getId();
        if (reaction.getOnComment() != null)
            this.onCommentId = reaction.getOnComment().getId();
        if (reaction.getOnEvent() != null)
            this.onEventId = reaction.getOnEvent().getId();
    }
}

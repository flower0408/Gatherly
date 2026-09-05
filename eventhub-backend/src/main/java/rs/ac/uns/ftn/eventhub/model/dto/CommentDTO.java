package rs.ac.uns.ftn.eventhub.model.dto;

import rs.ac.uns.ftn.eventhub.model.entity.Comment;
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
public class CommentDTO {

    private Long id = -1L;

    @NotBlank
    private String text;

    @NotBlank
    private String timestamp;

    private Long repliesToCommentId;

    @NotNull
    private Long belongsToUserId;

    private Long belongsToEventId;

    // Ime autora se dopisuje u kontroleru, radi prikaza uz komentar
    private String authorUsername;

    public CommentDTO(Comment comment) {
        this.id = comment.getId();
        this.text = comment.getText();
        this.timestamp = comment.getTimestamp().toString();
        if (comment.getRepliesTo() != null)
            this.repliesToCommentId = comment.getRepliesTo().getId();
        this.belongsToUserId = comment.getBelongsToUser().getId();
        if (comment.getBelongsToEvent() != null)
            this.belongsToEventId = comment.getBelongsToEvent().getId();
    }
}

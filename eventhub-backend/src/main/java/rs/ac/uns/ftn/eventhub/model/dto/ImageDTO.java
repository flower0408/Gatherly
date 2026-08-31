package rs.ac.uns.ftn.eventhub.model.dto;

import rs.ac.uns.ftn.eventhub.model.entity.Image;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImageDTO {

    private Long id = -1L;

    @NotBlank
    private String path;

    private Long belongsToEventId;

    private Long belongsToUserId;

    public ImageDTO(Image image) {
        this.id = image.getId();
        this.path = image.getPath();
        if (image.getBelongsToEvent() != null)
            this.belongsToEventId = image.getBelongsToEvent().getId();
        if (image.getBelongsToUser() != null)
            this.belongsToUserId = image.getBelongsToUser().getId();
    }
}

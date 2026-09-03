package rs.ac.uns.ftn.eventhub.model.dto;

import rs.ac.uns.ftn.eventhub.model.entity.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventDTO {

    private Long id = -1L;

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    @NotBlank
    private String location;

    @NotNull
    private String startsAt;

    @NotNull
    private Integer capacity;

    @NotNull
    private String creationDate;

    @NotNull
    private Long createdByUserId;

    private Long belongsToCommunityId;

    // Broj zauzetih mesta, racuna se iz prijava pa se dopisuje u kontroleru
    private Integer takenSpots;

    private List<ImageDTO> images;

    public EventDTO(Event createdEvent) {
        this.id = createdEvent.getId();
        this.title = createdEvent.getTitle();
        this.description = createdEvent.getDescription();
        this.location = createdEvent.getLocation();
        this.startsAt = createdEvent.getStartsAt().toString();
        this.capacity = createdEvent.getCapacity();
        this.creationDate = createdEvent.getCreationDate().toString();
        this.createdByUserId = createdEvent.getCreatedBy().getId();
    }
}

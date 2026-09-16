package rs.ac.uns.ftn.eventhub.model.dto;

import rs.ac.uns.ftn.eventhub.model.entity.Event;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
    private String endsAt;

    @NotNull
    private Integer capacity;

    // Kategorija se salje kao tekst, a u kontroleru se prevodi u EventCategory
    @NotBlank
    private String category;

    @NotNull
    private String creationDate;

    // Tvorac se uzima iz tokena, klijent ga ne salje
    private Long createdByUserId;

    private Long belongsToCommunityId;

    // Ime domacina se dopisuje u kontroleru: zajednica ako dogadjaj pripada nekoj,
    // inace onaj ko ga je otvorio. Tako se domacin vidi i kod samostalnih dogadjaja.
    private String hostName;

    // Broj zauzetih mesta, racuna se iz prijava pa se dopisuje u kontroleru
    private Integer takenSpots;

    private List<ImageDTO> images;

    public EventDTO(Event createdEvent) {
        this.id = createdEvent.getId();
        this.title = createdEvent.getTitle();
        this.description = createdEvent.getDescription();
        this.location = createdEvent.getLocation();
        this.startsAt = createdEvent.getStartsAt().toString();
        this.endsAt = createdEvent.getEndsAt().toString();
        this.capacity = createdEvent.getCapacity();
        this.category = createdEvent.getCategory().toString();
        this.creationDate = createdEvent.getCreationDate().toString();
        this.createdByUserId = createdEvent.getCreatedBy().getId();
    }
}

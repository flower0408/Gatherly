package rs.ac.uns.ftn.eventhub.model.dto;

import rs.ac.uns.ftn.eventhub.model.entity.EventRegistration;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventRegistrationDTO {

    private Long id = -1L;

    private String status;

    private String createdAt;

    private String at;

    @NotNull
    private Long createdByUserId;

    @NotNull
    private Long forEventId;

    // Ime i skor pouzdanosti ucesnika, dopisuju se u kontroleru za prikaz organizatoru
    private String participantUsername;

    private Integer participantReliability;

    // Upozorenje da se dogadjaj vremenski preklapa sa necim na sta je korisnik vec prijavljen
    private String conflictsWith;

    public EventRegistrationDTO(EventRegistration eventRegistration) {
        this.id = eventRegistration.getId();
        if (eventRegistration.getStatus() != null)
            this.status = eventRegistration.getStatus().toString();
        this.createdAt = eventRegistration.getCreatedAt().toString();
        if (eventRegistration.getAt() != null)
            this.at = eventRegistration.getAt().toString();
        this.createdByUserId = eventRegistration.getCreatedBy().getId();
        this.forEventId = eventRegistration.getForEvent().getId();
    }
}

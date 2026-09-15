package rs.ac.uns.ftn.eventhub.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.ftn.eventhub.model.entity.Report;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {

    private Long id;

    @NotBlank
    private String reason;

    @NotBlank
    private String timestamp;

    @NotNull
    private Long byUserId;

    private Boolean accepted;

    private Long onUserId;

    private Long onEventId;

    private Long onCommentId;

    // Kratak opis prijavljene stvari i dogadjaj na kome se nalazi, dopisuju se u kontroleru.
    // Bez toga bi onaj ko odlucuje video samo vrstu sadrzaja, a ne i o cemu se radi.
    private String targetLabel;

    private Long targetEventId;

    public ReportDTO(Report report){
        this.id = report.getId();
        this.reason = report.getReason().toString();
        this.timestamp = report.getTimestamp().toString();
        this.accepted = report.getAccepted();
        // Autor prijave moze da nedostaje ako je nalog obrisan
        if (report.getByUser() != null)
            this.byUserId = report.getByUser().getId();
        if (report.getOnUser() != null)
            this.onUserId = report.getOnUser().getId();
        if (report.getOnEvent() != null)
            this.onEventId = report.getOnEvent().getId();
        if (report.getOnComment() != null)
            this.onCommentId = report.getOnComment().getId();
    }


}

package rs.ac.uns.ftn.eventhub.model.entity;

import rs.ac.uns.ftn.eventhub.model.enums.ReportReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "report")
@SQLDelete(sql = "update report set is_deleted = true where id=?")
@Where(clause = "is_deleted = false")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportReason reason;

    @Column(nullable = false)
    private LocalDate timestamp;

    @ManyToOne
    @JoinColumn(name = "by_user_id", referencedColumnName = "id", nullable = false)
    private User byUser;

    @Column
    private Boolean accepted;

    @Column(nullable = false)
    private boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "on_user_id", referencedColumnName = "id")
    private User onUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "on_event_id", referencedColumnName = "id")
    private Event onEvent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "on_comment_id", referencedColumnName = "id")
    private Comment onComment;
}

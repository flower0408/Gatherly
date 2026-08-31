package rs.ac.uns.ftn.eventhub.model.entity;

import rs.ac.uns.ftn.eventhub.model.enums.ReactionType;
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
@Table(name = "reaction")
@SQLDelete(sql = "update reaction set is_deleted = true where id=?")
@Where(clause = "is_deleted = false")
public class Reaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReactionType type;

    @Column(nullable = false)
    private LocalDate timestamp;

    @Column(nullable = false)
    private boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "made_by_user_id", referencedColumnName = "id", nullable = false)
    private User madeBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "on_comment_id", referencedColumnName = "id")
    private Comment onComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "on_event_id", referencedColumnName = "id")
    private Event onEvent;
}

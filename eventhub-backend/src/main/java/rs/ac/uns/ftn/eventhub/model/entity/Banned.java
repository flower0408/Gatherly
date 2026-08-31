package rs.ac.uns.ftn.eventhub.model.entity;

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
@Table(name = "banned")
@SQLDelete(sql = "update banned set is_deleted = true where id=?")
@Where(clause = "is_deleted = false")
public class Banned {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "by_organizer_id", referencedColumnName = "id")
    private Organizer organizer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "towards_user_id", referencedColumnName = "id")
    private User towardsUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "for_community_id", referencedColumnName = "id")
    private Community community;

    @Column(nullable = false)
    private boolean isDeleted;
}

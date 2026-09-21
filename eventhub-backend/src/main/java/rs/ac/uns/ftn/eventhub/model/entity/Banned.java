package rs.ac.uns.ftn.eventhub.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "banned")
@SQLDelete(sql = "update banned set is_deleted = true where id=?")
@SQLRestriction("is_deleted = false")
public class Banned {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate timestamp;

    // Ko je izrekao blokadu: organizator zajednice ili administrator sistema
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "banned_by_user_id", referencedColumnName = "id", nullable = false)
    private User bannedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "towards_user_id", referencedColumnName = "id", nullable = false)
    private User towardsUser;

    // Prazno znaci blokada na nivou celog sistema, inace vazi samo za tu zajednicu
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "for_community_id", referencedColumnName = "id")
    private Community community;

    @Column(nullable = false)
    private boolean isDeleted;
}

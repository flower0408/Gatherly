package rs.ac.uns.ftn.eventhub.model.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import javax.persistence.*;

@Entity
@Table(name = "organizer")
@SQLDelete(sql = "update organizer set is_deleted = true where id=?")
@Where(clause = "is_deleted = false")
@Setter
@Getter
@RequiredArgsConstructor
public class Organizer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_id", referencedColumnName = "id", nullable = false)
    private Community community;

    @ColumnDefault("false")
    @Column(nullable = false)
    private boolean isDeleted;

}

package rs.ac.uns.ftn.eventhub.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "image")
@SQLDelete(sql = "update image set is_deleted = true where id=?")
@Where(clause = "is_deleted = false")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String path;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "belongs_to_event_id", referencedColumnName = "id")
    private Event belongsToEvent;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "belongs_to_user_id", referencedColumnName = "id")
    private User belongsToUser;

    @Column(nullable = false)
    private boolean isDeleted;
}

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
@Table(name = "comment")
@SQLDelete(sql = "update comment set is_deleted = true where id=?")
@Where(clause = "is_deleted = false")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String text;

    @Column(nullable = false)
    private LocalDate timestamp;

    @Column(nullable = false)
    private boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "belongs_to_user_id", referencedColumnName = "id", nullable = false)
    private User belongsToUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "belongs_to_event_id", referencedColumnName = "id")
    private Event belongsToEvent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "replies_to_comment_id", referencedColumnName = "id")
    private Comment repliesTo;
}

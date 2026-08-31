package rs.ac.uns.ftn.eventhub.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "community")
@SQLDelete(sql = "update community set is_deleted = true where id=?")
@Where(clause = "is_deleted = false")
public class Community {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private LocalDateTime creationDate;

    @Column(nullable = false)
    private boolean isSuspended;

    @Column
    private String suspendedReason;

    @Column(nullable = false)
    private boolean isDeleted;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "community_events", inverseJoinColumns=@JoinColumn(name="event_id"))
    private List<Event> communityEvents;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "community_organizers", inverseJoinColumns=@JoinColumn(name="organizer_id"))
    private List<User> communityOrganizers;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(name = "community_members", inverseJoinColumns=@JoinColumn(name="member_id"))
    private List<User> communityMembers;

}

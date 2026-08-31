package rs.ac.uns.ftn.eventhub.model.entity;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import rs.ac.uns.ftn.eventhub.model.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import javax.persistence.*;
import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "\"user\"")
@SQLDelete(sql = "update `user` set is_deleted = true where id=?")
@Where(clause = "is_deleted = false")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column
    private LocalDateTime lastLogin;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(name = "display_name")
    private String displayName;

    @Column
    private String description;

    @Column(nullable = false)
    private boolean isAdmin;

    @ColumnDefault("false")
    @Column(nullable = false)
    private boolean isVerified;

    @Column
    private String verificationToken;

    @ColumnDefault("false")
    @Column(nullable = false)
    private boolean isDeleted;
}

package rs.ac.uns.ftn.eventhub.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.ftn.eventhub.model.entity.User;
import rs.ac.uns.ftn.eventhub.model.enums.Role;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;

    @NotBlank
    private String username;

    @NotBlank
    private String password;

    @NotBlank
    private String email;

    private Role role;

    private String lastLogin;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    private String displayName;

    private String description;

    private boolean verified;

    private ImageDTO profileImage;

    public UserDTO(User createdUser) {
        this.id = createdUser.getId();
        this.username = createdUser.getUsername();
        this.email = createdUser.getEmail();
        this.role = createdUser.getRole();
        if (createdUser.getLastLogin() != null)
            this.lastLogin = createdUser.getLastLogin().toString();
        this.firstName = createdUser.getFirstName();
        this.lastName = createdUser.getLastName();
        this.displayName = createdUser.getDisplayName();
        this.description = createdUser.getDescription();
        this.verified = createdUser.isVerified();
    }
}

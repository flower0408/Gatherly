package rs.ac.uns.ftn.eventhub.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import rs.ac.uns.ftn.eventhub.model.entity.User;
import rs.ac.uns.ftn.eventhub.model.enums.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;

    @NotBlank
    private String username;

    // Ista donja granica kao pri promeni lozinke, da pravilo ne bude stroze posle registracije.
    // Prima se pri registraciji, ali se nikada ne vraca u odgovoru.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank
    @Size(min = 8, message = "The password must have at least 8 characters.")
    private String password;

    // Bez ispravne adrese nalog ne bi mogao da primi link za aktivaciju, pa nikad ne bi ni proradio
    @NotBlank
    @Email(message = "Please enter a valid email address.")
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

    // Skor pouzdanosti i broj dogadjaja iz kojih je izracunat, dopisuju se u kontroleru.
    // Broj se salje da bi front znao kada istorija jos nije dovoljna da se skor prikaze.
    private Integer reliability;

    private Integer attendanceCount;

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

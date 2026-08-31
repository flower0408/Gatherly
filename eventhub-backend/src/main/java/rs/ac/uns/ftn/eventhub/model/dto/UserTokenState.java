package rs.ac.uns.ftn.eventhub.model.dto;

// DTO koji enkapsulira generisani JWT i njegovo trajanje koji se vracaju klijentu
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserTokenState {

    private String accessToken;
    private Long expiresIn;
}

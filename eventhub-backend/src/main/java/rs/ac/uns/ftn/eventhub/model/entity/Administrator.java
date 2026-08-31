package rs.ac.uns.ftn.eventhub.model.entity;

import lombok.*;
import javax.persistence.*;
import java.util.*;
@RequiredArgsConstructor
@Getter
@Setter
@DiscriminatorValue("ADMIN")
public class Administrator extends User {

    @OneToMany
    private Set<Banned> bans = new HashSet<>();

}

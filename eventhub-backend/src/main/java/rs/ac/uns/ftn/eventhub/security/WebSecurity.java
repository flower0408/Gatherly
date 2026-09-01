package rs.ac.uns.ftn.eventhub.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import rs.ac.uns.ftn.eventhub.model.entity.User;
import rs.ac.uns.ftn.eventhub.service.UserService;

import javax.servlet.http.HttpServletRequest;

// Provera koja se poziva iz @PreAuthorize izraza i vidi @PathVariable vrednosti iz putanje,
// pa moze da uporedi korisnika sa zapisom nad kojim radi
@Component
public class WebSecurity {

    @Autowired
    private UserService userService;

    public boolean checkUserId(Authentication authentication, HttpServletRequest request, Long id) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        User user = userService.findByUsername(userDetails.getUsername());
        if (id.equals(user.getId())) {
            return true;
        }
        return false;
    }
}

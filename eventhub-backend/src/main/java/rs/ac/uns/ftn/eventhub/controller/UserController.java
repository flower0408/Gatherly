package rs.ac.uns.ftn.eventhub.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import rs.ac.uns.ftn.eventhub.model.dto.*;
import rs.ac.uns.ftn.eventhub.model.entity.Image;
import rs.ac.uns.ftn.eventhub.model.entity.User;
import rs.ac.uns.ftn.eventhub.security.PasswordPolicy;
import rs.ac.uns.ftn.eventhub.security.TokenUtils;
import rs.ac.uns.ftn.eventhub.service.BannedService;
import rs.ac.uns.ftn.eventhub.service.EventRegistrationService;
import rs.ac.uns.ftn.eventhub.service.ImageService;
import rs.ac.uns.ftn.eventhub.service.MailService;
import rs.ac.uns.ftn.eventhub.service.UserService;
import rs.ac.uns.ftn.eventhub.service.implementation.BannedServiceImpl;
import rs.ac.uns.ftn.eventhub.service.implementation.EventRegistrationServiceImpl;
import rs.ac.uns.ftn.eventhub.service.implementation.ImageServiceImpl;
import rs.ac.uns.ftn.eventhub.service.implementation.UserServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.Principal;
import java.time.LocalDateTime;


@RestController
@RequestMapping("api/users")
public class UserController {


    UserService userService;


    UserDetailsService userDetailsService;


    MailService mailService;


    ImageService imageService;


    BannedService bannedService;


    EventRegistrationService registrationService;


    AuthenticationManager authenticationManager;


    TokenUtils tokenUtils;


    PasswordPolicy passwordPolicy;

    private static final Logger logger = LogManager.getLogger(UserController.class);

    private static final int NAJVISE_PROMASAJA = 5;

    private static final int MINUTA_ZAKLJUCAVANJA = 15;

    @Autowired
    public UserController(UserServiceImpl userService, AuthenticationManager authenticationManager,
                          UserDetailsService userDetailsService, MailService mailService,
                          ImageServiceImpl imageService, BannedServiceImpl bannedService,
                          EventRegistrationServiceImpl registrationService, TokenUtils tokenUtils,
                          PasswordPolicy passwordPolicy) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.mailService = mailService;
        this.imageService = imageService;
        this.bannedService = bannedService;
        this.registrationService = registrationService;
        this.tokenUtils = tokenUtils;
        this.passwordPolicy = passwordPolicy;
    }

    // Uz korisnika se salje i njegova profilna slika, da front ne bi za svakog slao poseban zahtev
    private UserDTO toDTO(User user) {
        UserDTO userDTO = new UserDTO(user);
        Image profileImage = imageService.findProfileImageForUser(user.getId());
        if (profileImage != null)
            userDTO.setProfileImage(new ImageDTO(profileImage));
        userDTO.setReliability(registrationService.calculateReliability(user.getId()));
        userDTO.setAttendanceCount(registrationService.countAttendanceRecords(user.getId()));
        return userDTO;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> create(@RequestBody @Validated UserDTO newUser) {
        String passwordProblem = passwordPolicy.check(newUser.getPassword(), newUser.getUsername(), newUser.getEmail());
        if (passwordProblem != null) {
            logger.error("Password rejected for new user: " + newUser.getUsername());
            return new ResponseEntity<>(passwordProblem, HttpStatus.BAD_REQUEST);
        }
        logger.info("Creating user from DTO");
        User createdUser = userService.createUser(newUser);
        if (createdUser == null) {
            logger.error("User couldn't be created from DTO");
            return new ResponseEntity<>(HttpStatus.NOT_ACCEPTABLE);
        }
        logger.info("Sending verification mail to new user");
        mailService.sendVerificationMail(createdUser);
        logger.info("Creating response");
        UserDTO userDTO = toDTO(createdUser);
        logger.info("Created and sent response");

        return new ResponseEntity<>(userDTO, HttpStatus.CREATED);
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verify(@RequestParam("token") String token) {
        logger.info("Verifying account with token: " + token);
        User verifiedUser = userService.verifyUser(token);
        if (verifiedUser == null) {
            logger.error("No account found for verification token: " + token);
            return new ResponseEntity<>("This confirmation link is invalid or has already been used.", HttpStatus.BAD_REQUEST);
        }
        logger.info("Account of user with id: " + verifiedUser.getId() + " is now active");

        return new ResponseEntity<>("Your account has been activated. You can now sign in.", HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtAuthenticationRequest authenticationRequest) {
        User requestingUser = userService.findByUsername(authenticationRequest.getUsername());
        // Nalog koji je privremeno zakljucan zbog uzastopnih promasaja se ne proverava dalje
        if (requestingUser != null && isLocked(requestingUser)) {
            logger.error("Account of user with id: " + requestingUser.getId() + " is temporarily locked");
            return new ResponseEntity<>("Too many failed attempts. This account is locked for "
                    + MINUTA_ZAKLJUCAVANJA + " minutes.", HttpStatus.FORBIDDEN);
        }

        // Ukoliko kredencijali nisu ispravni, desice se AuthenticationException,
        // pa se promasaj broji i nalog se posle nekoliko pokusaja privremeno zakljucava
        logger.info("Checking user's username and password");
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    authenticationRequest.getUsername(), authenticationRequest.getPassword()));
        } catch (AuthenticationException exception) {
            if (requestingUser != null)
                countFailedAttempt(requestingUser);
            logger.error("Failed sign in for username: " + authenticationRequest.getUsername());
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // Nalog koji nije potvrdjen preko linka iz mejla ne moze da se prijavi
        logger.info("Checking if user's account is verified");
        if (!requestingUser.isVerified()) {
            logger.error("Account of user with id: " + requestingUser.getId() + " is not verified");
            return new ResponseEntity<>("This account has not been activated yet. "
                    + "Open the link we sent you when you signed up.", HttpStatus.FORBIDDEN);
        }
        // Blokiran korisnik ne moze da se prijavi na sistem
        if (bannedService.isBannedFromSystem(requestingUser.getId())) {
            logger.error("User with id: " + requestingUser.getId() + " is banned from the system");
            return new ResponseEntity<>("This account has been suspended and cannot be used any more.",
                    HttpStatus.FORBIDDEN);
        }
        logger.info("Putting user in security context");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        logger.info("Creating token for user");
        UserDetails user = (UserDetails) authentication.getPrincipal();
        String jwt = tokenUtils.generateToken(user);
        long expiresIn = tokenUtils.getExpiredIn();
        // Vreme prijave se upisuje tek posle uspesne provere, da neuspeli
        // pokusaj ne bi izgledao kao pristup nalogu
        logger.info("Setting last login time for user");
        User loggedInUser = userService.findByUsername(authenticationRequest.getUsername());
        loggedInUser.setLastLogin(LocalDateTime.now());
        // Uspesna prijava brise racun promasaja
        loggedInUser.setFailedLoginAttempts(0);
        loggedInUser.setLockedUntil(null);
        userService.saveUser(loggedInUser);

        logger.info("Created and sent response");
        return ResponseEntity.ok(new UserTokenState(jwt, expiresIn));
    }

    @GetMapping("/logout")
    public ResponseEntity logoutUser() {
        logger.info("Getting authentication from security context");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        logger.info("Checking if authentication is anonymous");
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            SecurityContextHolder.clearContext();
            logger.info("User successfully logged out");
            return new ResponseEntity("You have successfully logged out!", HttpStatus.OK);
        }

        logger.error("User is not authenticated and can't be logged out");
        return new ResponseEntity("User is not authenticated!", HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserDTO> getOne(@PathVariable Long userId, @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        String cleanToken = token.substring(7); //izbacivanje 'Bearer' iz tokena
        String username = tokenUtils.getUsernameFromToken(cleanToken); //izvlacenje username-a iz tokena
        User user = userService.findByUsername(username); //provera da li postoji u bazi
        if (user == null) {
            logger.error("User not found for token: " + cleanToken);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        logger.info("Finding user with id: " + userId);
        User findUser = userService.findById(userId);
        if (findUser == null) {
            logger.error("User not found with id: " + userId);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        UserDTO userDTO = toDTO(findUser);
        logger.info("Created and sent response");

        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }

    @GetMapping("/user/{queryUsername}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserDTO> getOneByUsername(@PathVariable String queryUsername, @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        String cleanToken = token.substring(7);
        String username = tokenUtils.getUsernameFromToken(cleanToken);
        User user = userService.findByUsername(username);
        if (user == null) {
            logger.error("User not found with token: " + cleanToken);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        logger.info("Finding user with username: " + queryUsername);
        User findUser = userService.findByUsername(queryUsername);
        if (findUser == null) {
            logger.error("User not found with username: " + queryUsername);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        logger.info("Creating response");
        UserDTO userDTO = toDTO(findUser);
        logger.info("Created and sent response");

        return new ResponseEntity<>(userDTO, HttpStatus.OK);
    }

    @PatchMapping("/edit")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserDTO> editUser(@RequestBody UserDTO editedUser, @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        String cleanToken = token.substring(7);
        String username = tokenUtils.getUsernameFromToken(cleanToken);
        User user = userService.findByUsername(username);
        if (user == null) {
            logger.error("User not found with token: " + cleanToken);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        logger.info("Finding original user with id: " + editedUser.getId());
        User oldUser = userService.findById(editedUser.getId());
        if (oldUser == null) {
            logger.error("Original user not found with id: " + editedUser.getId());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        // Svoj profil menja samo njegov vlasnik
        if (!oldUser.getId().equals(user.getId())) {
            logger.error("User with id: " + user.getId() + " tried to edit profile of user with id: " + oldUser.getId());
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        logger.info("Applying changes of user");
        if (editedUser.getFirstName() != null && !editedUser.getFirstName().isBlank())
            oldUser.setFirstName(editedUser.getFirstName());
        if (editedUser.getLastName() != null && !editedUser.getLastName().isBlank())
            oldUser.setLastName(editedUser.getLastName());
        if (editedUser.getDisplayName() != null)
            oldUser.setDisplayName(editedUser.getDisplayName());
        if (editedUser.getDescription() != null)
            oldUser.setDescription(editedUser.getDescription());
        oldUser = userService.saveUser(oldUser);
        logger.info("Creating response");
        UserDTO updatedUser = toDTO(oldUser);
        logger.info("Created and sent response");

        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }

    @PostMapping("/change-password")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<?> changePassword(@RequestBody @Validated UpdatePasswordDTO changePasswordRequest,
            @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        String cleanToken = token.substring(7);
        String username = tokenUtils.getUsernameFromToken(cleanToken);
        User user = userService.findByUsername(username);
        if (user == null) {
            logger.error("User not found with token: " + cleanToken);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        logger.info("Comparing hashes in request and database");
        String oldPassRequest = changePasswordRequest.getOldPassword();
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (!passwordEncoder.matches(oldPassRequest, user.getPassword())) {
            logger.error("Hashes do not match");
            return new ResponseEntity<>("Your current password is not correct.", HttpStatus.BAD_REQUEST);
        }
        String passwordProblem = passwordPolicy.check(changePasswordRequest.getNewPassword(),
                user.getUsername(), user.getEmail());
        if (passwordProblem != null) {
            logger.error("New password rejected for user with id: " + user.getId());
            return new ResponseEntity<>(passwordProblem, HttpStatus.BAD_REQUEST);
        }
        // Ista lozinka nije promena, a korisniku bi izgledalo kao da jeste
        if (passwordEncoder.matches(changePasswordRequest.getNewPassword(), user.getPassword())) {
            logger.error("New password is the same as the old one");
            return new ResponseEntity<>("The new password must be different from the current one.",
                    HttpStatus.BAD_REQUEST);
        }
        logger.info("Updating password for user with id: " + user.getId());
        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        user = userService.saveUser(user);
        logger.info("Sending notification mail about changed password");
        mailService.sendPasswordChangedMail(user);

        logger.info("You have successfully updated your password");
        return new ResponseEntity<>(toDTO(user), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id, @RequestHeader("authorization") String token) {
        logger.info("Authentication check");
        String cleanToken = token.substring(7);
        String username = tokenUtils.getUsernameFromToken(cleanToken);
        User user = userService.findByUsername(username);
        if (user == null) {
            logger.error("User not found with token: " + cleanToken);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        User userToDelete = userService.findById(id);
        // Nalog gasi njegov vlasnik ili administrator, niko treci
        if (userToDelete != null && !userToDelete.getId().equals(user.getId()) && !user.isAdmin()) {
            logger.error("User with id: " + user.getId() + " tried to delete account of user with id: " + id);
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if (userToDelete != null) {
            userService.deleteUser(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/whoami")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserDTO> whoAmI(Principal principal) {
        logger.info("Finding user from security context");
        User user = this.userService.findByUsername(principal.getName());
        if (user == null) {
            logger.error("User not found with username: " + principal.getName());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        logger.info("Created and sent response");

        return new ResponseEntity<>(toDTO(user), HttpStatus.OK);
    }

    // Uzastopni promasaji se broje, a posle zadatog broja nalog se zakljucava na kratko.
    // NIST SP 800-63B trazi ogranicavanje uzastopnih neuspelih pokusaja prijave.
    private boolean isLocked(User user) {
        if (user.getLockedUntil() == null)
            return false;
        if (user.getLockedUntil().isAfter(LocalDateTime.now()))
            return true;
        // Vreme zakljucavanja je isteklo, pa racun promasaja krece ispocetka
        user.setLockedUntil(null);
        user.setFailedLoginAttempts(0);
        userService.saveUser(user);
        return false;
    }

    private void countFailedAttempt(User user) {
        int attempts = user.getFailedLoginAttempts() == null ? 0 : user.getFailedLoginAttempts();
        attempts = attempts + 1;
        user.setFailedLoginAttempts(attempts);
        if (attempts >= NAJVISE_PROMASAJA) {
            logger.error("Locking account of user with id: " + user.getId()
                    + " after " + attempts + " failed attempts");
            user.setLockedUntil(LocalDateTime.now().plusMinutes(MINUTA_ZAKLJUCAVANJA));
        }
        userService.saveUser(user);
    }
}

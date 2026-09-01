package rs.ac.uns.ftn.eventhub.service.implementation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import rs.ac.uns.ftn.eventhub.model.dto.UserDTO;
import rs.ac.uns.ftn.eventhub.model.enums.Role;
import rs.ac.uns.ftn.eventhub.model.entity.User;
import rs.ac.uns.ftn.eventhub.repository.UserRepository;
import rs.ac.uns.ftn.eventhub.service.UserService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {


    private UserRepository userRepository;


    private PasswordEncoder passwordEncoder;


    @Autowired
    public UserServiceImpl(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder){
        this.passwordEncoder = passwordEncoder;
    }

    private static final Logger logger = LogManager.getLogger(UserServiceImpl.class);

    @Override
    public User findByUsername(String username) {
        Optional<User> user = userRepository.findFirstByUsername(username);
        if (!user.isEmpty())
            return user.get();
        logger.error("Repository search for user with username: " + username + " returned null");
        return null;
    }

    @Override
    public User findById(Long id) {
        return userRepository.findByIdAndIsDeletedFalse(id);
    }

    @Override
    public User createUser(UserDTO userDTO) {

        Optional<User> user = userRepository.findFirstByUsername(userDTO.getUsername());

        if(user.isPresent()){
            logger.error("User with username: " + userDTO.getUsername() + " already exists in repository");
            return null;
        }

        Optional<User> userWithEmail = userRepository.findFirstByEmail(userDTO.getEmail());

        if(userWithEmail.isPresent()){
            logger.error("User with email: " + userDTO.getEmail() + " already exists in repository");
            return null;
        }

        User newUser = new User();
        newUser.setUsername(userDTO.getUsername());
        newUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        newUser.setEmail(userDTO.getEmail());
        newUser.setFirstName(userDTO.getFirstName());
        newUser.setLastName(userDTO.getLastName());
        newUser.setAdmin(false);
        newUser.setDeleted(false);
        newUser.setVerified(false);
        newUser.setVerificationToken(UUID.randomUUID().toString());
        newUser.setRole(Role.USER);
        newUser = userRepository.save(newUser);

        return newUser;
    }

    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public User verifyUser(String verificationToken) {
        Optional<User> user = userRepository.findFirstByVerificationToken(verificationToken);

        if (user.isEmpty()) {
            logger.error("Repository search for user with verification token: " + verificationToken + " returned null");
            return null;
        }

        User foundUser = user.get();
        logger.info("Activating account of user with id: " + foundUser.getId());
        foundUser.setVerified(true);
        foundUser.setVerificationToken(null);

        return userRepository.save(foundUser);
    }

    @Override
    public Integer deleteUser(Long id) {
        return userRepository.deleteUserById(id);
    }

    @Override
    public List<User> searchUsersByNames(String firstName, String lastName) {
        Optional<List<User>> users = userRepository.findUsersByFirstAndLastName(firstName, lastName);
        if (!users.isEmpty())
            return users.get();
        logger.error("Repository search for users with provided query returned null");
        return null;
    }

    @Override
    public List<User> findAll() {
        return this.userRepository.findAllActiveUsers().orElse(Collections.emptyList());
    }
}

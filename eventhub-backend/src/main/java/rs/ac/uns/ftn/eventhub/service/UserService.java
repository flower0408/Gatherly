package rs.ac.uns.ftn.eventhub.service;

import rs.ac.uns.ftn.eventhub.model.dto.UserDTO;
import rs.ac.uns.ftn.eventhub.model.entity.User;

import java.util.List;

public interface UserService {

    User findByUsername(String username);

    User findById(Long id);

    User createUser(UserDTO userDTO);

    User saveUser(User user);

    User verifyUser(String verificationToken);

    Integer deleteUser(Long id);

    List<User> searchUsersByNames(String firstName, String lastName);

    List<User> findAll();

}

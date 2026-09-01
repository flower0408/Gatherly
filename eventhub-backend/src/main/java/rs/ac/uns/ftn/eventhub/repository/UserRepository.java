package rs.ac.uns.ftn.eventhub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.eventhub.model.entity.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByIdAndIsDeletedFalse(Long id);

    Optional<User> findFirstByUsername(String username);

    Optional<User> findFirstByEmail(String email);

    Optional<User> findFirstByVerificationToken(String verificationToken);

    @Query(nativeQuery = true,
            value = "select * from `user` " +
                    "where (first_name like concat('%', :firstName, '%') or last_name like concat('%', :firstName, '%') or " +
                    "first_name like concat('%', :lastName, '%') or last_name like concat('%', :lastName, '%')) " +
                    "and is_deleted = false;")
    Optional<List<User>> findUsersByFirstAndLastName(@Param("firstName") String firstName, @Param("lastName") String lastName);

    @Transactional
    @Modifying
    Integer deleteUserById(Long id);

    @Query(nativeQuery = true,
            value = "select * from `user` where is_deleted = false;")
    Optional<List<User>> findAllActiveUsers();

}

package rs.ac.uns.ftn.eventhub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import rs.ac.uns.ftn.eventhub.model.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByIdAndIsDeletedFalse(Long id);

    Optional<User> findFirstByUsername(String username);

    Optional<User> findFirstByEmail(String email);

    // Korisnicko ime i adresa ostaju zauzeti i posle mekog brisanja, jer red ostaje u bazi.
    // Anotacija @SQLRestriction sakriva obrisane redove, pa se ovde ide nativnim upitom.
    @Query(nativeQuery = true, value = "select count(*) from `user` where username = :username")
    Integer countByUsernameIncludingDeleted(@Param("username") String username);

    @Query(nativeQuery = true, value = "select count(*) from `user` where email = :email")
    Integer countByEmailIncludingDeleted(@Param("email") String email);

    Optional<User> findFirstByVerificationToken(String verificationToken);

    @Transactional
    @Modifying
    Integer deleteUserById(Long id);

}

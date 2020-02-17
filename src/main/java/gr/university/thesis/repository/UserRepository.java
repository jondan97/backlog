package gr.university.thesis.repository;

import gr.university.thesis.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository that manages everything that has to do with a user
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * @param email: unique identifier for each user
     * @return: returns an optional that contains a unique user
     */
    Optional<User> findFirstByEmail(String email);

    @Query("SELECT user FROM User user JOIN user.roles role WHERE role.role=:roleName")
    List<User> findByUserRole(@Param("roleName") String roleName);
}

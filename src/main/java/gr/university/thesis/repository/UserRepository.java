package gr.university.thesis.repository;

import gr.university.thesis.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

/**
 * Repository that manages everything that has to do with a user
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * @param email: unique identifier for each user
     * @return : returns an optional that contains a unique user
     */
    Optional<User> findFirstByEmail(String email);

    /**
     * custom query made to fetch all the users but the admin from the repository
     *
     * @param roleName: role of the required users, could be something like: "fetch me all the users with role ADMIN"
     *                  for example
     * @return : a set of all users fulfilling the requirements specified above
     */
    @Query("SELECT user FROM User user JOIN user.roles role WHERE role.role=:roleName")
    Set<User> findByUserRole(@Param("roleName") String roleName);
}

package gr.university.thesis.repository;

import gr.university.thesis.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository that manages everything that has to do with a user
 */
@Repository
public interface UserRepository extends CrudRepository<User, Long> {
    /**
     * @param email: unique identifier for each user
     * @return: returns an optional that contains a unique user
     */
    Optional<User> findByEmail(String email);
}

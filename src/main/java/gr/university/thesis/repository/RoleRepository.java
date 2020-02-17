package gr.university.thesis.repository;

import gr.university.thesis.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository that manages everything that has to do with user roles
 */
public interface RoleRepository extends JpaRepository<Role, Long> {
}

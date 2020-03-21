package gr.university.thesis.repository;

import gr.university.thesis.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository that manages everything that has to do with the projects of the system
 */
public interface ProjectRepository extends JpaRepository<Project, Long> {
    /**
     * returns the first project that it finds with a certain id
     *
     * @param id: unique identifier for each project
     * @return : returns an optional that contains a unique project
     */
    Optional<Project> findFirstById(long id);

    /**
     * returns the first project that it finds with a certain title
     *
     * @param title: title of the project, should be unique
     * @return : returns an optional that may contain the requested project
     */
    Optional<Project> findFirstByTitle(String title);
}

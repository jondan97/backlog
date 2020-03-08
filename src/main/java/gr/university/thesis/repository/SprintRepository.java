package gr.university.thesis.repository;

import gr.university.thesis.entity.Project;
import gr.university.thesis.entity.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repository that manages everything that has to do with sprints
 */

public interface SprintRepository extends JpaRepository<Sprint, Long> {

    /**
     * @param status: the status of the sprint, read 'status' attribute in Sprint class for more info
     * @return: returns an optional that contains the sprint that was requested or returns an empty optional if it
     * doesn't exist
     */
    Optional<Sprint> findFirstByProjectAndStatus(Project project, byte status);

    /**
     * this method returns a unique sprint that belongs to a project
     *
     * @param sprintId:  the id of the sprint that the user requested to find
     * @param projectId: the id of the project that the sprint belongs to
     * @return return an optional that may contain the sprint requested
     */
    @Query("SELECT sprint FROM Sprint sprint WHERE sprint.id=:sprintId AND sprint.project.id=:projectId")
    Optional<Sprint> findDistinctSprintByProjectId(@Param("sprintId") long sprintId, @Param("projectId") long projectId);
}
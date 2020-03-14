package gr.university.thesis.repository;

import gr.university.thesis.entity.Project;
import gr.university.thesis.entity.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository that manages everything that has to do with sprints
 */

public interface SprintRepository extends JpaRepository<Sprint, Long> {

    /**
     * this method searches the repository to find the first sprint with a certain status, mostly used to find the
     * active sprint
     *
     * @param status: the status of the sprint, read 'status' attribute in Sprint class for more info
     * @return: returns an optional that contains the sprint that was requested or returns an empty optional if it
     * doesn't exist
     */
    Optional<Sprint> findFirstByProjectAndStatus(Project project, byte status);

    /**
     * this method searches the repository for all the sprints with a certain status, will mainly be used to fetch
     * the finished sprints in each project (history)
     *
     * @param status: the status of the sprint, read 'status' attribute in Sprint class for more info
     * @return: returns an optional that contains the list of sprints that was requested or returns an empty optional if
     * no sprint with the requested status doesn't exist
     */
    Optional<List<Sprint>> findByProjectAndStatus(Project project, byte status);

    /**
     * this method returns a unique sprint that belongs to a project
     *
     * @param projectId: the id of the project that the sprint belongs to
     * @param sprintId:  the id of the sprint that the user requested to find
     * @return return an optional that may contain the sprint requested
     */
    @Query("SELECT sprint FROM Sprint sprint WHERE sprint.project.id=:projectId AND sprint.id=:sprintId")
    Optional<Sprint> findDistinctSprintByProjectId(@Param("projectId") long projectId, @Param("sprintId") long sprintId);

}
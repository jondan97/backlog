package gr.university.thesis.repository;

import gr.university.thesis.entity.Item;
import gr.university.thesis.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Repository that manages everything that has to do with items
 */
public interface ItemRepository extends JpaRepository<Item, Long> {

    /**
     * this method returns all the items that belong to a project
     *
     * @param projectId: the id of the project that these items belong to
     * @return return an iterable with all the items
     */
    Iterable<Item> findByProjectIdOrderByPriorityDesc(long projectId);

    Iterable<Item> findByProjectIdAndStatusOrderByPriorityDesc(long projectId, byte itemStatus);

    /**
     * this method takes as input a title and a project, and returns an item that belongs to that project and has
     * that certain title from the repository
     *
     * @param title:   the title that the item has
     * @param project: the project that this item belongs to
     * @return : returns an optional that may contain an item with that certain title
     */
    Optional<Item> findFirstByTitleAndProject(String title, Project project);

    /**
     * this method returns a unique item that belong to a project
     *
     * @param itemId:    the id of the item that the user requested to find
     * @param projectId: the id of the project that the item belongs to
     * @return return an optional that may contain the item requested
     */
    @Query("SELECT item FROM Item item WHERE item.id=:itemId AND item.project.id=:projectId")
    Optional<Item> findDistinctItemByProjectId(@Param("itemId") long itemId, @Param("projectId") long projectId);


}

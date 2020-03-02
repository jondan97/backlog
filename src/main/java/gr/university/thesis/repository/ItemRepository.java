package gr.university.thesis.repository;

import gr.university.thesis.entity.Item;
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
    Iterable<Item> findByProjectId(long projectId);

    /**
     * this method returns a unique that belong to a project
     *
     * @param projectId: the id of the project that these items belong to
     * @return return an iterable with all the items
     */
    Iterable<Item> findFirstByProjectId(long projectId);

    @Query("SELECT item FROM Item item WHERE item.id=:itemId AND item.project.id=:projectId")
    Optional<Item> findDistinctItemByProjectId(@Param("itemId") long itemId, @Param("projectId") long projectId);
}

package gr.university.thesis.repository;

import gr.university.thesis.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

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
}

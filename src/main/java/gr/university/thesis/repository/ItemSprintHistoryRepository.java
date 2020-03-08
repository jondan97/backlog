package gr.university.thesis.repository;

import gr.university.thesis.entity.Item;
import gr.university.thesis.entity.ItemSprintHistory;
import gr.university.thesis.entity.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository that manages everything that has to do with the history between an item and a sprint
 */
public interface ItemSprintHistoryRepository extends JpaRepository<ItemSprintHistory, Long> {
    /**
     * @param item:   the item that is associated with the sprint
     * @param sprint: the sprint that is associated with the item
     * @return: returns an optional that might contain the association between item and sprint
     */
    Optional<ItemSprintHistory> findFirstByItemAndSprint(Item item, Sprint sprint);
}

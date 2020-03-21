package gr.university.thesis.repository;

import gr.university.thesis.entity.Item;
import gr.university.thesis.entity.ItemSprintHistory;
import gr.university.thesis.entity.Sprint;
import gr.university.thesis.entity.enumeration.TaskBoardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Repository that manages everything that has to do with the history between an item and a sprint
 */
public interface ItemSprintHistoryRepository extends JpaRepository<ItemSprintHistory, Long> {
    /**
     * @param item:   the item that is associated with the sprint
     * @param sprint: the sprint that is associated with the item
     * @return : returns an optional that might contain the association between item and sprint
     */
    Optional<ItemSprintHistory> findFirstByItemAndSprint(Item item, Sprint sprint);


    /**
     * this method takes as input two types (at max) of item (bug and task for example), and returns all the items
     * that belongs to a certain sprint and have a certain task board status (for example 'done'), the developer chose
     * two item types, because at the time of creating this method, they needed a way to fetch both tasks and bugs,
     * and instead of using 2 queries for it, they used only one
     *
     * @param sprintId:  the sprint that the items belong to
     * @param statusId:  the status that we are looking for (for example 'to_do',in_progress etc.)
     * @param itemType1: type of the item1, for example bug or task
     * @param itemType2: type of the item2,
     * @return : returns an optional that may contain a list with items of two types (at max)
     */
    @Query("SELECT ish FROM ItemSprintHistory ish WHERE ish.sprint.id=:sprintId " +
            "AND ish.status=:statusId " +
            "AND (ish.item.type=:itemType1 " +
            "OR ish.item.type=:itemType2) " +
            "ORDER BY ish.last_moved")
    Optional<List<ItemSprintHistory>> findAllBySprintAndStatusAndItemTypes(@Param("sprintId") long sprintId,
                                                                           @Param("statusId") TaskBoardStatus statusId,
                                                                           @Param("itemType1") int itemType1,
                                                                           @Param("itemType2") int itemType2);


    /**
     * this method is mainly used to calculate the total effort of a sprint, it takes all the items and sums the effort
     * of all the tasks and bugs that are contained in this sprint
     *
     * @param sprintId   : the sprint that this association belongs to
     * @param itemType1: type of the item1, for example bug or task
     * @param itemType2: type of the item2,
     * @return returns an optional that may contain a list with items of two types (at max)
     */
    @Query("SELECT ish FROM ItemSprintHistory ish WHERE ish.sprint.id=:sprintId " +
            "AND (ish.item.type=:itemType1 " +
            "OR ish.item.type=:itemType2) " +
            "ORDER BY ish.last_moved")
    Optional<List<ItemSprintHistory>> findAllBySprintAndItemTypes(@Param("sprintId") long sprintId,
                                                                  @Param("itemType1") int itemType1,
                                                                  @Param("itemType2") int itemType2);

}



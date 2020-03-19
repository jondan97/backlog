package gr.university.thesis.entity;

import gr.university.thesis.entity.enumeration.TaskBoardStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

/**
 * this class tracks the status of an item for every sprint that it was associated with
 * when the user wants to check past sprints, the status of each item also needs to be known
 */
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ItemSprintHistory {

    /**
     * unique identifier, composite key of sprint and item
     */
    @EmbeddedId
    private SprintItemId sprintItemId;

    /**
     * sprint of the association
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("sprintId")
    private Sprint sprint;

    /**
     * item of the association
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("itemId")
    private Item item;

    /**
     * what was the status of the item when the sprint finished? What is the status of the item on the current sprint?
     */
    @Column
    private TaskBoardStatus status;

    /**
     * when was the last time this item had its status changed?
     */
    @Column
    private Date last_moved;

    /**
     * constructor that sets the association between an item and a sprint
     *
     * @param item    : item of the assocation
     * @param sprint  : sprint of the assocation
     * @param status: the status of the item in the sprint, check the respective enum class for more info
     */
    public ItemSprintHistory(Item item, Sprint sprint, TaskBoardStatus status) {
        this.item = item;
        this.sprint = sprint;
        this.status = status;
        sprintItemId = new SprintItemId();
    }

    /**
     * this method compares if two associations are equal by comparing their items and sprints (which in turn compare
     * with their ids)
     *
     * @param o: object that the user requested to compare this with
     * @return: returns true if associations are equal, false if not
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItemSprintHistory)) return false;
        ItemSprintHistory that = (ItemSprintHistory) o;
        return getSprint().equals(that.getSprint()) &&
                getItem().equals(that.getItem());
    }

    /**
     * this method converts the association to a set of numbers, mainly used for integrity reasons
     *
     * @return: returns the hashcode of the association which is an int
     */
    @Override
    public int hashCode() {
        return Objects.hash(getSprint(), getItem());
    }

    /**
     * this method is mainly used for debugging reasons
     *
     * @return: returns a string with a set of attributes of an association
     */
    @Override
    public String toString() {
        return "ItemSprintHistory{" +
                "sprint=" + sprint +
                ", item=" + item +
                ", status=" + status +
                ", last_moved=" + last_moved +
                '}';
    }
}

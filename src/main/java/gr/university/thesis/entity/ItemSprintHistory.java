package gr.university.thesis.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

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
    private int status;

    /**
     * constructor that sets the association between an item and a sprint
     *
     * @param item    : item of the assocation
     * @param sprint  : sprint of the assocation
     * @param status: the status of the item in the sprint, check the respective enum class for more info
     */
    public ItemSprintHistory(Item item, Sprint sprint, int status) {
        this.item = item;
        this.sprint = sprint;
        this.status = status;
        sprintItemId = new SprintItemId();
    }
}

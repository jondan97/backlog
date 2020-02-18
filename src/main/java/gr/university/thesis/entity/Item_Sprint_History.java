package gr.university.thesis.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * this class tracks the status of an item for every sprint that it was associated with
 * when the user wants to check past sprints, the status of each item also needs to be known
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Item_Sprint_History {

    /**
     * unique identifier, composite key of sprint and item
     */
    @EmbeddedId
    private SprintItemId sprintItemId;

    /**
     * sprint of the association
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("sprint_id")
    private Sprint sprint;

    /**
     * item of the association
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("item_id")
    private Item item;

    /**
     * what was the status of the item when the sprint finished? What is the status of the item on the current sprint?
     */
    @Column
    private int status;
}

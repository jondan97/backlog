package gr.university.thesis.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * class that is being used as an @Embeddable type to hold the composite entity identifier of the history repository
 * table between an item and a sprint,
 */
@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SprintItemId implements Serializable {

    /**
     * id of the sprint that belongs to the association
     */
    @Column
    private Long sprint_id;

    /**
     * id of the item that belongs to the assocation
     */
    @Column
    private Long item_id;

    //maybe an equals is needed to check for both ids (reminder to check later if it works)
}

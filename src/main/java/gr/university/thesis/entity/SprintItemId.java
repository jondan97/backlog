package gr.university.thesis.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

/**
 * class that is being used as an @Embeddable type to hold the composite entity identifier of the history repository
 * table between an item and a sprint,
 */
@Embeddable
@Getter
@Setter
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

    //maybe an equals is needed to check for both ids (reminder to check later if it works) ----> UPDATE: 1 month later,
    // yes I needed a hashCode method...

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SprintItemId that = (SprintItemId) o;
        return Objects.equals(sprint_id, that.sprint_id) &&
                Objects.equals(item_id, that.item_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sprint_id, item_id);
    }
}

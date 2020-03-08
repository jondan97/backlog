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
    @Column(name = "sprint_id")
    private Long sprintId;

    /**
     * id of the item that belongs to the assocation
     */
    @Column(name = "item_id")
    private Long itemId;

    //maybe an equals is needed to check for both ids (reminder to check later if it works) ----> UPDATE: 1 month later,
    // yes I needed a hashCode method...

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SprintItemId that = (SprintItemId) o;
        return Objects.equals(sprintId, that.sprintId) &&
                Objects.equals(itemId, that.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sprintId, itemId);
    }
}

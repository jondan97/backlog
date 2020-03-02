package gr.university.thesis.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

/**
 * users can leave comments below items, for other users to read
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Comment {

    /**
     * unique identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * what the user has actually commented
     */
    @Column
    private String body;

    /**
     * when the comment was created
     */
    @Column
    private Date date_created;

    /**
     * who is the owner of the comment
     */
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /**
     * which item the comment was written for
     */
    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private Item item;

    /**
     * custom constructor
     */
    public Comment(String body, Item item, User owner) {
        this.body = body;
        this.item = item;
        this.owner = owner;
        this.date_created = new Date();
    }
}

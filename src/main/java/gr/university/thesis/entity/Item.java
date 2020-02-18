package gr.university.thesis.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

/**
 * items/tasks that can be added to a backlog or a sprint
 */
@Entity
@Data
public class Item {

    /**
     * unique identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * what is the item's type, is it an Epic, a Bug etc.
     */
    @Column
    private int type;

    /**
     * the name of the item
     */
    @Column
    private String title;

    /**
     * the description of the item
     */
    @Column
    private String description;

    /**
     * the effort that is required to finish the item
     */
    @Column
    private int effort;

    /**
     * the priority of the item, is it important or low priority?
     */
    @Column
    private int priority;

    /**
     * the parent of the item, for example an epic can have many child items
     */
    @ManyToOne
    private Item parent;

    /**
     * the child items that belong to a parent item, for example an epic
     */
    @OneToMany(mappedBy = "parent")
    private Set<Item> children;

    /**
     * when the item was created
     */
    @Column
    private Date date_created;

    /**
     * what project this item belongs to, this is used in case the item remains in the backlog of a project and not
     * put into a sprint
     */
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /**
     * who created this item
     */
    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /**
     * who this item is assigned to
     */
    @ManyToOne
    @JoinColumn(name = "assignee_id", nullable = false)
    private User assignee;

    /**
     * is this item active or not (will see if I need this in the future)
     */
    @Column
    private boolean active;

    /**
     * the set of comments this item contains
     */
    @OneToMany(mappedBy = "item")
    private Set<Comment> comments;

    /**
     * the set of sprints this item is associated with
     */
    @OneToMany(mappedBy = "item")
    private Set<Item_Sprint_History> associatedSprints;
}

package gr.university.thesis.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * items/tasks that can be added to a backlog or a sprint
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
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
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
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
     * the list of comments this item contains
     */
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private List<Comment> comments;

    /**
     * the set of sprints this item is associated with
     */
    @OneToMany(mappedBy = "item")
    private Set<Item_Sprint_History> associatedSprints;

    /**
     * custom constructor
     */
    public Item(String title, String description, int type, int priority, int effort, Project project, User assignee, User owner, Item parent) {
        this.title = title;
        this.description = description;
        this.type = type;
        this.priority = priority;
        this.effort = effort;
        this.project = project;
        this.assignee = assignee;
        this.owner = owner;
        this.active = false;
        this.date_created = new Date();
        this.parent = parent;
    }

    /**
     * this constructor was created as some sort of 'setter'
     *
     * @param itemId: the id of the comment
     */
    public Item(long itemId) {
        this.id = itemId;
    }
}

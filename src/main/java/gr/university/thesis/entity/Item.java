package gr.university.thesis.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.List;
import java.util.Objects;
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
     * the acceptance criteria of the item (when it is considered done by the team's standards)
     */
    @Column
    private String acceptanceCriteria;

    /**
     * the effort that is required to finish the item
     */
    @Column
    private int effort;

    /**
     * the estimated effort that is required to finish the item
     */
    @Column
    private int estimatedEffort;

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
     * 0: finished item
     * 1: item in backlog
     * 2: item is included in ready sprint
     * 3: item is included in active sprint
     */
    @Column
    private byte status;

    /**
     * the list of comments this item contains
     */
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private List<Comment> comments;

    /**
     * the set of sprints this item is associated with
     */
    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private Set<ItemSprintHistory> associatedSprints;

    /**
     * custom constructor
     */
    public Item(String title, String description, String acceptanceCriteria, int type, int priority, int effort, int estimatedEffort, Project project, User assignee, User owner, Item parent) {
        this.title = title;
        this.description = description;
        this.acceptanceCriteria = acceptanceCriteria;
        this.type = type;
        this.priority = priority;
        this.effort = effort;
        this.estimatedEffort = estimatedEffort;
        this.project = project;
        this.assignee = assignee;
        this.owner = owner;
        //ready to be taken from backlog
        this.status = 1;
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


    /**
     * this method compares if two items are equal by comparing their ids
     *
     * @param o: the object we want to compare it with
     * @return : returns true if items are equal, false if not
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Item)) return false;
        Item item = (Item) o;
        return getId().equals(item.getId());
    }

    /**
     * this method converts the item to a set of numbers, mainly used for integrity reasons
     *
     * @return : returns the hashcode of the item which is an int
     */
    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    /**
     * this method is mainly used for debugging reasons
     *
     * @return : returns a string with a set of attributes of an item
     */
    @Override
    public String toString() {
        return "Item{" +
                "id=" + id +
                ", type=" + type +
                ", title='" + title + '\'' +
                ", effort=" + effort +
                ", status=" + status +
                '}';
    }
}

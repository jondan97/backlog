package gr.university.thesis.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.Set;

/**
 * the project that contains the backlog and the sprints
 */
@Entity
@Data
public class Project {

    /**
     * unique identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * name of the project
     */
    @Column
    private String title;

    /**
     * description of the project
     */
    @Column
    private String description;

    /**
     * the total effort of the project (will see if needed in the future), cached version of the total effort
     * so that it is not calculated every time the user requests it
     */
    @Column
    private long total_effort;

    /**
     * the associated sprints
     */
    @OneToMany(mappedBy = "project")
    private Set<Sprint> sprints;

    /**
     * the associated items that are in the backlog
     */
    @OneToMany(mappedBy = "project")
    private Set<Item> items;

    /**
     * the set of users that belong to this project
     */
    @ManyToMany(mappedBy = "belongingProjects")
    Set<User> users;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;
}

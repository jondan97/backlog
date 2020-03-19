package gr.university.thesis.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

/**
 * the project that contains the backlog and the sprints
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
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
     * developers that will work on this project
     */
    @Column
    private int developers_working;

    /**
     * estimated sprints needed in order for the project to finish (rough estimation)
     */
    @Column
    private int estimated_sprints_needed;

    /**
     * the total effort of the project calculated every time the user wants to see this project in the project panel etc.
     */
    @Transient
    private long total_effort;

    /**
     * the associated sprints
     */
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private Set<Sprint> sprints;

    /**
     * the associated items that are in the backlog
     */
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private Set<Item> items;

    /**
     * the set of users that belong to this project
     */
    @ManyToMany(mappedBy = "belongingProjects")
    Set<User> users;

    /**
     * the owner of this project
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    /**
     * @param title:                  the title of the project
     * @param description:            the description of the project
     * @param developersWorking:      the number of developers working for that project
     * @param estimatedSprintsNeeded: the number of sprints estimated in order to complete the project
     * @param owner:                  the owner of tis project
     */
    public Project(String title, String description, int developersWorking, int estimatedSprintsNeeded, User owner) {
        this.title = title;
        this.description = description;
        this.developers_working = developersWorking;
        this.estimated_sprints_needed = estimatedSprintsNeeded;
        this.owner = owner;
    }

    /**
     * this constructor was created as some sort of 'setter'
     *
     * @param projectId: the id of the project
     */
    public Project(long projectId) {
        this.id = projectId;
    }
}

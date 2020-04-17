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
     * the velocity of the team, during the execution of sprints
     */
    @Column
    private int team_velocity;

    /**
     * the duration of each sprint, counted in weeks
     */
    @Column
    private int sprint_duration;


    /**
     * the total effort of the project calculated every time the user wants to see this project in the project panel etc.
     */
    @Transient
    private long total_effort;

    /**
     * the remaining effort needed to complete the project, calculated with the following formula:
     * total effort - effort of items with status done
     */
    @Transient
    private long remaining_effort;

    /**
     * the estimated total effort of the project calculated every time the user wants to see this project
     * in the project panel etc., it counts all the stray stories and epics
     */
    @Transient
    private long estimated_total_effort;

    /**
     * the estimated sprints needed, calculated with the following formula:
     * total_effort / team_velocity
     */
    @Transient
    private long estimated_sprints_needed;

    /**
     * the executed sprints that have happened during this project, does not include current sprint running
     */
    @Transient
    private long executed_sprints;

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
     * @param title:             the title of the project
     * @param description:       the description of the project
     * @param developersWorking: the number of developers working for that project
     * @param teamVelocity:      the number of sprints estimated in order to complete the project
     * @param sprint_duration    : the duration of each sprint in the project
     * @param owner:             the owner of tis project
     */
    public Project(String title, String description, int developersWorking, int teamVelocity, int sprint_duration, User owner) {
        this.title = title;
        this.description = description;
        this.developers_working = developersWorking;
        this.team_velocity = teamVelocity;
        this.sprint_duration = sprint_duration;
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

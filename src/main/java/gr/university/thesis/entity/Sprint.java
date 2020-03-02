package gr.university.thesis.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

/**
 * the sprint that is associated with a project, and contains items
 */
@Entity
@Getter
@Setter
public class Sprint {

    /**
     * unique identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * the duration of the sprint
     */
    @Column
    private long duration;

    /**
     * the start date of the sprint
     */
    @Column
    private Date start_date;

    /**
     * the end date of the sprint
     */
    @Column
    private Date end_date;

    /**
     * the goal of the sprint, what does the team want to achieve by finishing this sprint
     */
    @Column
    private String goal;

    /**
     * the total effort of the sprint, (will see if needed in the future), cached version of the total effort
     * so that it is not calculated every time the user requests it
     */
    @Column
    private long total_effort;

    /**
     * how fast is the project going, its velocity, calculated on the end of the sprint (most likely)
     */
    @Column
    private int velocity;

    /**
     * is the sprint the current active one, or is it a finished sprint
     */
    @Column
    private boolean active;

    /**
     * the project that this sprint is associated with
     */
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    /**
     * the associated items that are included in this sprint
     */
    @OneToMany(mappedBy = "sprint")
    private Set<Item_Sprint_History> associatedItems;

}

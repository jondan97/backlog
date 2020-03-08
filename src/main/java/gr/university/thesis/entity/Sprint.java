package gr.university.thesis.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
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
@NoArgsConstructor
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
    @Transient
    private long total_effort;

    /**
     * how fast is the project going, its velocity, calculated on the end of the sprint (most likely)
     */
    @Transient
    private int velocity;

    /**
     * the days remaining for this project: calculated by subtracting the starting date from the end date
     */
    @Transient
    private int days_remaining;

    /**
     * 0: finished sprint
     * 1: currect active sprint
     * 2: ready to start sprint
     */
    @Column
    private byte status;

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
    private Set<ItemSprintHistory> associatedItems;

    /**
     * mostly used during initialisation of project
     *
     * @param project: project that this sprint belongs to
     * @param status:  the status, which most likely will be 2
     */
    public Sprint(Project project, byte status) {
        this.project = project;
        this.status = status;
    }

    /**
     * constructor works as a 'setter'
     *
     * @param sprintId: id of the sprint
     */
    public Sprint(long sprintId) {
        this.id = sprintId;
    }
}

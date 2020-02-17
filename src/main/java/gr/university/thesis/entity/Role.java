package gr.university.thesis.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * Role that is saved in the repository
 */
@Entity
@Data
public class Role {

    /**
     * unique identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * role that represents each user, for example it could be ADMIN or USER
     */
    @Column
    private String role;
}

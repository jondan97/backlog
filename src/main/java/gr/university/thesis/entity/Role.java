package gr.university.thesis.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

/**
 * Role that is saved in the repository
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
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

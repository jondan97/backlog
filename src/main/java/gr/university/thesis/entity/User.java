package gr.university.thesis.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Set;

/**
 * User that is saved in the repository
 */
@Entity
@Data
@NoArgsConstructor
public class User {

    /**
     * unique identifier
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    /**
     * uniquely identified, used for a user to log into the system
     */
    private String email;

    /**
     * password that matches to user when required to login
     */
    @Column
    private String password;

    /**
     * first name that matches to user
     */
    @Column
    private String firstName;

    /**
     * last name that matches to user
     */
    @Column
    private String lastName;

    /**
     * many to many relationship with the Role class, each user can have one or more roles
     */
    @ManyToMany(fetch = FetchType.EAGER)
    //user owns the association, role does not own the association
    @JoinTable(name = "role_user", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles;

    @OneToMany(mappedBy = "owner")
    private Set<Item> owningItems;

    @OneToMany(mappedBy = "assignee")
    private Set<Item> assignedItems;

    @OneToMany(mappedBy = "owner")
    private Set<Comment> comments;

    /**
     * this constructor is being used when a new user is being created
     *
     * @param email:     newly added email
     * @param password:  newly added password
     * @param firstName: newly added first name of user
     * @param lastName:  newly added last name of user
     */
    public User(String email, String password, String firstName, String lastName) {
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }
}

package gr.university.thesis.entity;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

/**
 * Class used by Spring to determine the user details of each user for a session, for authentication/authorization
 */
public class SessionUser extends User {

    private Long id;

    /**
     * @param id:                    the unique identifier used in the repository
     * @param username:              in this case, our system uses an email in order to login and it is also unique
     * @param password:              associated with each user, when trying to login
     * @param enabled:               is the user enabled (active) or not
     * @param accountNonExpired:     has the account expired
     * @param credentialsNonExpired: have the credentials expired
     * @param accountNonLocked:      is the account locked
     * @param authorities:           the set of roles associated with each user
     */
    public SessionUser(Long id, String username, String password, boolean enabled, boolean accountNonExpired, boolean credentialsNonExpired, boolean accountNonLocked, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.id = id;
    }

    /**
     * @return :returns the id of the session user
     */
    public Long getId() {
        return id;
    }
}

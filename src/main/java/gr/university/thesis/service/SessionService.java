package gr.university.thesis.service;

import gr.university.thesis.entity.SessionUser;
import gr.university.thesis.entity.User;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

/**
 * service that is associated with the management of the session
 */
@Service
public class SessionService {

    /**
     * this method takes as input a user and a session, and sets user attributes to the existing session
     *
     * @param user:    the user that is associated with the current session
     * @param session: the session that the attributes will be set on
     */
    public void setSessionAttributes(SessionUser user, HttpSession session) {
        session.setAttribute("userId", user.getId());
        session.setAttribute("userEmail", user.getUsername());
        session.setAttribute("role", this.findRole(user));
    }

    /**
     * this method instantly finds the repository id of an user
     *
     * @param session: the current session with a user
     * @return : returns a User that only contains the Id saved in an attribute of the session
     */
    public User getUserWithSessionId(HttpSession session) {
        return new User((long) session.getAttribute("userId"));
    }

    /**
     * this method takes as input a user and returns a role in the form of a String
     *
     * @param user: the user that is associated with the current session
     * @return : returns the highest privilege role that is associated with the user
     */
    private String findRole(SessionUser user) {
        //roles: user OR admin
        List<String> authorityList = new ArrayList<>(user.getAuthorities().size());
        //for each authority, get the String authority (role) and add it to the String list
        user.getAuthorities().forEach(authority -> authorityList.add(authority.getAuthority()));
        //since the role-user association is implemented as a ManyToMany relationship,
        // we need to return the highest rank
        if (authorityList.contains("MASTER_ADMIN"))
            return "Master Admin";
        else if (authorityList.contains("ADMIN"))
            return "Admin";
        else if (authorityList.contains("PRODUCT_OWNER")) {
            return "Product Owner";
        } else if (authorityList.contains("SCRUM_MASTER")) {
            return "Scrum Master";
        } else {
            return "Developer";
        }
    }

    /**
     * this method checks if the user has logged in during the session or not
     *
     * @param authentication: received from current session of user
     * @return returns true if user is already logged in, false if not
     */
    public boolean isUserLoggedIn(Authentication authentication) {
        if (authentication instanceof AnonymousAuthenticationToken) {
            return false;
        } else {
            return true;
        }
    }


}

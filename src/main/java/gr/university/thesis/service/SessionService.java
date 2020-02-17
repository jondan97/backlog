package gr.university.thesis.service;

import gr.university.thesis.entity.SessionUser;
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
     * this method takes as input a user and returns a role in the form of a String
     *
     * @param user: the user that is associated with the current session
     * @return: returns the highest privilege role that is associated with the user
     */
    private String findRole(SessionUser user) {
        //roles: user OR admin
        List<String> authorityList = new ArrayList<>(user.getAuthorities().size());
        //for each authority, get the String authority (role) and add it to the String list
        user.getAuthorities().forEach(authority -> authorityList.add(authority.getAuthority()));
        if (authorityList.contains("ADMIN"))
            return "ADMIN";
        else
            return "USER";
    }


}

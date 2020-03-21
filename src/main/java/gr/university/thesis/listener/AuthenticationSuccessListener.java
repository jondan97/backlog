package gr.university.thesis.listener;

import gr.university.thesis.entity.SessionUser;
import gr.university.thesis.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;

/**
 * This class allows the system to listen to login events, mainly used to record successful logins from users
 * and setting their session attributes for usage all over the system
 */
@Component
public class AuthenticationSuccessListener implements ApplicationListener<AuthenticationSuccessEvent> {

    private HttpSession session;
    private SessionService sessionService;

    /**
     * constructor of this class, correct way to set the autowired attributes
     *
     * @param session:       the http session that is associated with the user
     * @param sessionService : service that manages the session with the user
     */
    @Autowired
    public AuthenticationSuccessListener(HttpSession session, SessionService sessionService) {
        this.session = session;
        this.sessionService = sessionService;
    }

    /**
     * this method listens to successful logins by users, and sets their session attributes
     * so that they can be used all over the system
     *
     * @param event: an event triggered by the user (login successful)
     */
    @Override
    public void onApplicationEvent(AuthenticationSuccessEvent event) {
        //find the session user
        SessionUser user = (SessionUser) event.getAuthentication().getPrincipal();
        //set the attributes
        sessionService.setSessionAttributes(user, session);
    }
}

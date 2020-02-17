package gr.university.thesis.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * This class allows the system to listen to login events, mainly used to capture the email of a user that failed to
 * login and passing it to the redirection towards the login page
 */
@Component
public class LoginListener implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    private HttpServletRequest request;

    /**
     * constructor of this class, correct way to set the autowired attributes
     *
     * @param request: the http request received from a user
     */
    @Autowired
    public LoginListener(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * this method listens for application events, and specifically for bad credentials requests
     *
     * @param event: an event triggered by the user (login failed)
     */
    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event) {
        //get the email that the user attempted to login with
        Object emailObj = event.getAuthentication().getPrincipal();
        //get the string email
        String email = emailObj.toString();
        //set for the upcoming session, the session of the request received
        HttpSession session = request.getSession();
        //set the userEmail attribute of the session
        session.setAttribute("userEmail", email);
    }

    /**
     * this method allows the 'passing' of the email, that the user attempted to login with, during the redirection
     * towards the login page. This allows a user to have the email readily shown again after a bad credentials
     * attempt
     *
     * @param model:   the user interface that the email will be shown on
     * @param session: the current user session
     */
    public void passEmail(Model model, HttpSession session) {
        //if the session email attribute is not empty, then:
        if (!(session.getAttribute("userEmail") == null)) {
            //receive the attribute
            String email = session.getAttribute("userEmail").toString();
            //delete the attribute (you don't want the attribute to remain if the user refreshes the page for example)
            session.removeAttribute("userEmail");
            //add it to the UI
            model.addAttribute("email", email);
        }
    }
}
package gr.university.thesis.controller;

import gr.university.thesis.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

/**
 * This is the general controller, here requests are of a more general nature, have almost nothing to do
 * with user/admin use cases
 */
@Controller
public class GeneralController {

    UserDetailsServiceImpl userDetailsServiceImpl;


    /**
     * constructor of this class, correct way to set the autowired attributes
     *
     * @param userDetailsServiceImpl class used to load users into the user and role repositories
     */
    @Autowired
    public GeneralController(UserDetailsServiceImpl userDetailsServiceImpl) {
        this.userDetailsServiceImpl = userDetailsServiceImpl;
    }

    /** shows the main page of the system
     * @return main page
     */
    @GetMapping("/")
    public String mainPage() {
        return "main";
    }

    /** this is where the user is required to login, or is redirected here when attempting to do an unauthorized action
     * @param model:   user interface
     * @param session: session communicated with the user
     * @return login page
     */
    @GetMapping("/login")
    public String login(Model model,
                        HttpSession session
    ) {
        return "login";
    }

    /** used in order to initialize some data needed for the system to properly work, always run this first
     * if system is run for the first time
     *
     * @return redirection to a page (probably main page)
     */
    @GetMapping("/firstTime")
    public String createUsers() {
        userDetailsServiceImpl.firstTime();
        return "redirect:/";
    }
}

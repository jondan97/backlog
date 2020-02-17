package gr.university.thesis.controller;

import gr.university.thesis.listener.LoginListener;
import gr.university.thesis.repository.RoleRepository;
import gr.university.thesis.repository.UserRepository;
import gr.university.thesis.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

@Controller
public class GeneralController {

    UserRepository userRepository;
    RoleRepository roleRepository;
    LoginListener loginListener;
    UserDetailsServiceImpl userDetailsServiceImpl;


    /**
     * constructor of this class, correct way to set the autowired attributes
     *
     * @param userRepository         repository used to access a user
     * @param roleRepository         repository used to access roles of a user
     * @param loginListener          class used to listen to login events, mainly listening for user email
     * @param userDetailsServiceImpl class used to load users into the user and role repositories
     */
    @Autowired
    public GeneralController(UserRepository userRepository, RoleRepository roleRepository, LoginListener loginListener, UserDetailsServiceImpl userDetailsServiceImpl) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.loginListener = loginListener;
        this.userDetailsServiceImpl = userDetailsServiceImpl;
    }

    /**
     * shows the main page of the system
     *
     * @return main page
     */
    @GetMapping("/")
    public String mainPage() {
        return "main";
    }

    /**
     * this is where the user is required to login, or is redirected here when attempting to do an unauthorized action
     *
     * @param model:   user interface
     * @param session: session communicated with the user
     * @return login page
     */
    @GetMapping("/login")
    public String login(Model model,
                        HttpSession session
    ) {
        loginListener.passEmail(model, session);
        return "login";
    }

    @GetMapping("/test")
    public String testPage() {
        return "main";
    }

    /**
     * used in order to initialize some data needed for the system to properly work, always run this first
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

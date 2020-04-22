package gr.university.thesis.controller;

import gr.university.thesis.exceptions.*;
import gr.university.thesis.service.InitiationService;
import gr.university.thesis.service.SessionService;
import gr.university.thesis.service.SprintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * This is the general controller, here requests are of a more general nature, have almost nothing to do
 * with user/admin use cases
 */
@Controller
public class GeneralController {

    InitiationService initiationService;
    SessionService sessionService;
    SprintService sprintService;


    /**
     * constructor of this class, correct way to set the autowired attributes
     *
     * @param initiationService: service that initialises some essential data needed for the application to run
     * @param sessionService     : service that manages the user session
     */
    @Autowired
    public GeneralController(InitiationService initiationService, SessionService sessionService, SprintService sprintService) {
        this.sessionService = sessionService;
        this.initiationService = initiationService;
        this.sprintService = sprintService;
    }

    /** shows the main page of the system
     * @param phaseStr: this is needed because for some reason, Hibernate bugs and when sprint.getAssociatedItems() is
     *                called, it fetches no items even though they exist in the repository, since no actual solution was
     *                found, a workaround was made that 'refreshes' the firstTime() method and the fetching does not
     *                bring null back
     */
    @GetMapping("/")
    public String mainPage(@RequestParam(required = false) String phaseStr) throws ProjectHasEmptyTitleException, ProjectAlreadyExistsException, ItemAlreadyExistsException, ItemHasEmptyTitleException, SprintHasZeroEffortException {
        int phase = 0;
        boolean isFirstTime;
        if (phaseStr == null || phaseStr.equals("one")) {
            phase = 1;
        } else if (phaseStr.equals("two")) {
            phase = 2;
        }
        //used in order to initialize some data needed for the system to properly work, always run this first
        //if system is run for the first time
        isFirstTime = initiationService.firstTime(phase);
        if (isFirstTime) {
            return "redirect:/?phaseStr=two";
        } else {
            return "main";
        }
    }

    /**
     * this is where the user is required to login, or is redirected here when attempting to do an unauthorized action
     *
     * @return login page
     */
    @GetMapping("/login")
    public String login() {
        boolean loggedIn;
        loggedIn = sessionService.isUserLoggedIn(SecurityContextHolder.getContext().getAuthentication());
        if (loggedIn) {
            /* The user is logged in */
            return "redirect:/";
        } else {
            return "login";
        }
    }

    /**
     * used in order for a user to logout from the session and therefore losing his authentication
     *
     * @return redirects to login page
     */
    @GetMapping("/logout")
    public String logout() {
        return "redirect:/login?logout";
    }
}

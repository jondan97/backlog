package gr.university.thesis.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpSession;

/**
 * This is the user controller, here requests are coming from authorized users, for example create project
 */
@Controller
@RequestMapping("/user")
public class UserController {

    /**
     * 'main' page of user, here, the user(s) can manage all the project of the system
     *
     * @param model:   user interface that is shown to user
     * @param session: session required to get the current's admin ID (only the admin can access this method so we
     *                 find the session's userId attribute
     * @return: returns the manageUsers template
     */
    @GetMapping("/manageProjects")
    public String manageUsers(Model model, HttpSession session) {
        //instantly getting the user id, which in all cases (hopefully if security works properly) is the admin
        //we don't want the admin to be able to
        //Set<User> users = userService.findUsersByRole("USER", (long) session.getAttribute("userId"));
        //Set<User> users = userService.findUsersByRole("USER");
        //if (!users.isEmpty()) {
        //    model.addAttribute("users", users);
        // }
        return "manageUsers";
    }
}

package gr.university.thesis.controller;

import gr.university.thesis.entity.User;
import gr.university.thesis.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * This controller has to do with all the use cases relating to the admin of the system
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    UserService userService;

    /**
     * constructor of this class, correct way to set the autowired attributes
     *
     * @param userService: service that manages all the users of the system
     */
    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 'main' page of admin, here, the admin can manage all the users of the system
     *
     * @param model: user interface that is shown to user
     * @return: returns the manageUsers template
     */
    @GetMapping("/manageUsers")
    public String manageUsers(Model model) {
        List<User> users = userService.findUsersByRole("USER");
        if (!users.isEmpty()) {
            model.addAttribute("users", users);
        }
        return "manageUsers";
    }

    /**
     * this method calls the user service in order to create a new user and store him in the repository
     *
     * @param email:    input for the email of the new user
     * @param password: input for the password of the new user
     * @return: returns manageUsers template (redirects)
     */
    @PostMapping("/createUser")
    public String createUser(@RequestParam String email,
                             @RequestParam String password) {
        userService.createUser(email, password);
        return "redirect:/admin/manageUsers";
    }
}

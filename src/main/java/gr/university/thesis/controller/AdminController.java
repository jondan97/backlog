package gr.university.thesis.controller;

import gr.university.thesis.entity.User;
import gr.university.thesis.entity.enumeration.RoleEnum;
import gr.university.thesis.service.RoleService;
import gr.university.thesis.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * This controller has to do with all the use cases relating to the admin of the system
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    UserService userService;
    RoleService roleService;

    /**
     * constructor of this class, correct way to set the autowired attributes
     *
     * @param userService: service that manages all the users of the system
     * @param roleService: service that manages all the roles of the system
     */
    @Autowired
    public AdminController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    /**
     * 'main' page of admin, here, the admin can manage all the users of the system
     *
     * @param model:   user interface that is shown to user
     * @param session: session required to get the current's admin ID (only the admin can access this method so we
     *                 find the session's userId attribute
     * @return: returns the manageUsers template
     */
    @GetMapping("/userPanel")
    public String manageUsers(Model model, HttpSession session) {
        //instantly getting the user id, which in all cases (hopefully if security works properly) is the admin
        //we don't want the admin to be able to
        List<User> users = userService.findAllUsers();
        if (!users.isEmpty()) {
            model.addAttribute("users", users);
        }
        model.addAttribute("allRoleEnums", RoleEnum.values());
        return "userPanel";
    }

    /**
     * this method calls the user service in order to create a new user and store him in the repository
     *
     * @param email:     input for the email of the new user
     * @param password:  input for the password of the new user
     * @param firstName: input for the first name of the user
     * @param lastName:  input for the last name of the user
     * @param role:      input for the role of the user
     * @return: returns user panel template (redirects)
     */
    @PostMapping("/createUser")
    public String createUser(@RequestParam String email,
                             @RequestParam String password,
                             @RequestParam String firstName,
                             @RequestParam String lastName,
                             @RequestParam String role) {
        userService.createUser(email, password, firstName, lastName, role);
        return "redirect:/admin/userPanel";
    }

    /**
     * this method calls the user service in order to update an existing user and store him in the repository
     *
     * @param userId:       required id in order to fetch the user from the repository
     * @param userEmail:    input for the email of the user
     * @param userPassword: input for the password of the user
     * @param: userFirstName: input for the first name of the user
     * @param: userLastName: input for the last name of the user
     * @param userRole: input for the role of the user
     * @return: returns user panel template (redirects)
     */
    @RequestMapping(value = "/editUser", params = "action=update", method = RequestMethod.POST)
    public String updateUser(@RequestParam long userId,
                             @RequestParam String userEmail,
                             @RequestParam String userPassword,
                             @RequestParam String userFirstName,
                             @RequestParam String userLastName,
                             @RequestParam String userRole) {
        userService.updateUser(userId, userEmail, userPassword, userFirstName, userLastName, userRole);
        return "redirect:/admin/userPanel";
    }

    /**
     * this method calls the user service in order to delete an existing user from the repository
     *
     * @param userId: required id in order to delete the user from the repository
     * @return: returns user panel template (redirects)
     */
    @RequestMapping(value = "/editUser", params = "action=delete", method = RequestMethod.POST)
    public String deleteUser(@RequestParam long userId) {
        userService.deleteUser(userId);
        return "redirect:/admin/userPanel";
    }
}

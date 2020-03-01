package gr.university.thesis.controller;


import gr.university.thesis.entity.Project;
import gr.university.thesis.entity.enumeration.ItemPriority;
import gr.university.thesis.entity.enumeration.ItemType;
import gr.university.thesis.repository.ItemRepository;
import gr.university.thesis.service.ItemService;
import gr.university.thesis.service.ProjectService;
import gr.university.thesis.service.SessionService;
import gr.university.thesis.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

/**
 * This is the user controller, here requests are coming from authorized users, for example create project
 */
@Controller
@RequestMapping("/user")
public class UserController {

    ProjectService projectService;
    SessionService sessionService;
    UserService userService;
    ItemRepository itemRepository;
    ItemService itemService;

    /**
     * constructor of this class, correct way to set the autowired attributes
     *
     * @param projectService: service that manages all the projects of the system
     * @param sessionService: the service that manages the current session
     * @param userService:    service that manages all the users of the system
     * @param itemService:    service that handles all the items of the system
     */
    @Autowired
    public UserController(ProjectService projectService, SessionService sessionService, UserService userService, ItemService itemService) {
        this.projectService = projectService;
        this.sessionService = sessionService;
        this.userService = userService;
        this.itemService = itemService;
    }

    /**
     * this method shows to the user, the backlog and the sprints of a certain project
     *
     * @param projectId: the project id that was requested by the user to view
     * @param model:     the user interface that will be shown in the front-end
     * @return
     */
    @RequestMapping(value = "/project/{projectId}")
    public String viewProject(@PathVariable long projectId,
                              Model model) {
        Optional<Project> projectOptional = projectService.findProjectById(projectId);
        if (projectOptional.isPresent()) {
            model.addAttribute("project", projectOptional.get());
            //perhaps not the most sufficient but this application is not supposed to be scalable
            model.addAttribute("allUsers", userService.findAllUsers());
            model.addAttribute("backlog", itemService.findAllItemsByProjectId(projectId));
            model.addAttribute("itemTypes", ItemType.values());
            model.addAttribute("itemPriorities", ItemPriority.values());
        }
        return "project";
    }

}

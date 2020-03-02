package gr.university.thesis.controller;


import gr.university.thesis.entity.Item;
import gr.university.thesis.entity.Project;
import gr.university.thesis.entity.User;
import gr.university.thesis.entity.enumeration.ItemPriority;
import gr.university.thesis.entity.enumeration.ItemType;
import gr.university.thesis.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.List;
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
    CommentService commentService;
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
    public UserController(ProjectService projectService, SessionService sessionService, UserService userService, ItemService itemService, CommentService commentService) {
        this.projectService = projectService;
        this.sessionService = sessionService;
        this.userService = userService;
        this.itemService = itemService;
        this.commentService = commentService;
    }

    /**
     * 'main' page of user, here, the user(s) can manage all the project of the system
     *
     * @param model:   user interface that is shown to user
     * @param session: session required to get the current's admin ID (only the admin can access this method so we
     *                 find the session's userId attribute
     * @return: returns the manageUsers template
     */
    @GetMapping("/projectPanel")
    public String projectsPanel(Model model, HttpSession session) {
        List<Project> allProjects = projectService.findAllProjects();
        model.addAttribute("projects", allProjects);
        return "projectPanel";
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

    /**
     * this method shows to the user, the backlog and the sprints of a certain project
     *
     * @param projectId: the project id that was requested by the user to view
     * @param model:     the user interface that will be shown in the front-end
     * @return
     */
    @RequestMapping(value = "/project/{projectId}/item/{itemId}")
    public String viewItem(@PathVariable long projectId,
                           @PathVariable long itemId,
                           Model model) {
        Optional<Item> itemOptional = itemService.findItemInProject(itemId, projectId);
        if (itemOptional.isPresent()) {
            //we want the most recent one shown first, we sort our item comments using lambda, and then we reverse it
            Collections.sort(itemOptional.get().getComments(), Collections.reverseOrder((o1, o2) -> o1.getDate_created().compareTo(o2.getDate_created())));
            model.addAttribute("item", itemOptional.get());
            //the following two help in finding the enums associated with the item
            model.addAttribute("itemType", ItemType.findItemTypeByRepositoryId(itemOptional.get().getType()));
            model.addAttribute("itemPriority", ItemPriority.findItemTypeByRepositoryId(itemOptional.get().getPriority()));
        }
        return "item";
    }

    @RequestMapping(value = "/updateAssignee")
    public String updateAssignee(@RequestParam long itemId,
                                 @RequestParam long itemAssigneeId,
                                 @RequestParam long itemProjectId) {
        itemService.updateAssignee(itemId, new User(itemAssigneeId));
        return "redirect:/user/project/" + itemProjectId;
    }

    /**
     * this method calls the comment service in order to create a new comment and store it in the repository
     *
     * @param commentBody:   body of the comment, what is says
     * @param commentItemId: item that this belongs to
     * @param session:       the current session, needed to find the creator of the comment
     * @return: returns a redirection to the item of a project
     */
    @PostMapping("/createComment")
    public String createComment(@RequestParam String commentBody,
                                @RequestParam long commentItemId,
                                @RequestParam long commentProjectId,
                                HttpSession session) {
        commentService.createComment(commentBody, new Item(commentItemId), sessionService.getUserWithSessionId(session));
        return "redirect:/user/project/" + commentProjectId + "/item/" + commentItemId;
    }

    /**
     * this method calls the comment service in order to update an existing comment and store it in the repository
     *
     * @param commentIdView:     the comment id in order to find it on the repository
     * @param commentBodyView:   body of the comment, what is says
     * @param commentItemIdView: required for the redirection
     * @param commentBodyView:   required for the redirection
     * @return: returns a redirection to the item of a project
     */
    @RequestMapping(value = "/editComment", params = "action=update", method = RequestMethod.POST)
    public String updateComment(@RequestParam long commentIdView,
                                @RequestParam long commentItemIdView,
                                @RequestParam long commentProjectIdView,
                                @RequestParam String commentBodyView) {
        commentService.updateComment(commentIdView, commentBodyView);
        return "redirect:/user/project/" + commentProjectIdView + "/item/" + commentItemIdView;
    }

    /**
     * this method calls the comment service in order to delete an existing comment from the repository
     *
     * @param commentIdView:        required id in order to delete the comment from the repository
     * @param commentItemIdView:    required for the redirection
     * @param commentProjectIdView: required for the redirection
     * @return: returns a redirection to the item of a project
     */
    @RequestMapping(value = "/editComment", params = "action=delete", method = RequestMethod.POST)
    public String deleteComment(@RequestParam long commentIdView,
                                @RequestParam long commentItemIdView,
                                @RequestParam long commentProjectIdView
    ) {
        commentService.deleteComment(commentIdView);
        return "redirect:/user/project/" + commentProjectIdView + "/item/" + commentItemIdView;
    }

}

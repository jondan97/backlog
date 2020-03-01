package gr.university.thesis.controller;

import gr.university.thesis.entity.Project;
import gr.university.thesis.entity.User;
import gr.university.thesis.entity.enumeration.ItemPriority;
import gr.university.thesis.entity.enumeration.ItemType;
import gr.university.thesis.service.ProjectService;
import gr.university.thesis.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * This is the project manager controller, here, a project manager can request actions such as viewing all projects
 * or creating one
 */
@Controller
@RequestMapping("/pm")
public class ProjectManagerController {

    ProjectService projectService;
    SessionService sessionService;

    /**
     * constructor of this class, correct way to set the autowired attributes
     *
     * @param projectService: service that manages all the projects of the system
     * @param sessionService: the service that manages the current session
     */
    @Autowired
    public ProjectManagerController(ProjectService projectService, SessionService sessionService) {
        this.projectService = projectService;
        this.sessionService = sessionService;
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
     * this method calls the project service in order to create a new project and store it in the repository
     *
     * @param title:       input for the title of the new project
     * @param description: input for the description of the new project
     * @param session:     the current session, needed to find the creator of the project
     * @return: returns project panel template (redirects)
     */
    @PostMapping("/createProject")
    public String createProject(@RequestParam String title,
                                @RequestParam String description,
                                HttpSession session) {
        projectService.createProject(title, description, sessionService.getUserWithSessionId(session));
        return "redirect:/pm/projectPanel";
    }

    /**
     * this method calls the project service in order to update an existing project and store it in the repository
     *
     * @param projectId:          the project id in order to find it on the repository
     * @param projectTitle:       the possibly updated title of the project
     * @param projectDescription: the possibly updated description of the project
     * @return: returns project panel template (redirects)
     */
    @RequestMapping(value = "/editProject", params = "action=update", method = RequestMethod.POST)
    public String updateProject(@RequestParam long projectId,
                                @RequestParam String projectTitle,
                                @RequestParam String projectDescription) {
        projectService.updateProject(projectId, projectTitle, projectDescription);
        return "redirect:/pm/projectPanel";
    }

    /**
     * this method calls the project service in order to delete an existing project from the repository
     *
     * @param projectId: required id in order to delete the project from the repository
     * @return: returns project panel template (redirects)
     */
    @RequestMapping(value = "/editProject", params = "action=delete", method = RequestMethod.POST)
    public String deleteProject(@RequestParam long projectId) {
        projectService.deleteProject(projectId);
        return "redirect:/pm/projectPanel";
    }

    /**
     * this method calls the project service in order to create a new item and store it in the repository
     *
     * @param title:       input for the title of the new project
     * @param description: input for the description of the new project
     * @param type:        input for the type of item
     * @param priority:    input for the priority of the item
     * @param effort:      input for the effort needed to complete the item
     * @param projectId:   project that this item belongs to
     * @param assigneeId:  id of the assigned user
     * @param session:     current session, needed to find the creator of this item
     * @return
     */
    @PostMapping("/createItem")
    public String createItem(@RequestParam String title,
                             @RequestParam String description,
                             @RequestParam String type,
                             @RequestParam String priority,
                             @RequestParam int effort,
                             @RequestParam long projectId,
                             @RequestParam long assigneeId,
                             HttpSession session) {
        projectService.createItem(title, description, ItemType.valueOf(type),
                ItemPriority.valueOf(priority), effort, new Project(projectId),
                new User(assigneeId), sessionService.getUserWithSessionId(session));
        return "redirect:/user/project/" + projectId;
    }
}

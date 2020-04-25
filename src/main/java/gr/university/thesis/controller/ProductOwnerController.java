package gr.university.thesis.controller;

import gr.university.thesis.entity.Item;
import gr.university.thesis.entity.Project;
import gr.university.thesis.entity.User;
import gr.university.thesis.entity.enumeration.ItemPriority;
import gr.university.thesis.entity.enumeration.ItemType;
import gr.university.thesis.exceptions.ItemAlreadyExistsException;
import gr.university.thesis.exceptions.ItemHasEmptyTitleException;
import gr.university.thesis.exceptions.ProjectAlreadyExistsException;
import gr.university.thesis.exceptions.ProjectHasEmptyTitleException;
import gr.university.thesis.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

/**
 * This is the project manager controller, here, a project manager can request actions such as creating a project
 * or an item
 */
@Controller
@RequestMapping("/pm")
public class ProductOwnerController {

    ProjectService projectService;
    SessionService sessionService;
    ItemService itemService;
    SprintService sprintService;
    ItemSprintHistoryService itemSprintHistoryService;


    /**
     * constructor of this class, correct way to set the autowired attributes
     *
     * @param projectService:          service that manages all the projects of the system
     * @param sessionService:          service that manages the current session
     * @param itemService:             service that manages the items stored in the repository
     * @param sprintService:           service that manages the sprints stored in the repository
     * @param itemSprintHistoryService : services that manages all the associations between sprint and items
     */
    @Autowired
    public ProductOwnerController(ProjectService projectService, SessionService sessionService,
                                  ItemService itemService, SprintService sprintService,
                                  ItemSprintHistoryService itemSprintHistoryService) {
        this.projectService = projectService;
        this.sessionService = sessionService;
        this.itemService = itemService;
        this.sprintService = sprintService;
        this.itemSprintHistoryService = itemSprintHistoryService;
    }

    /**
     * this method calls the project service in order to create a new project and store it in the repository
     *
     * @param title:             input for the title of the new project
     * @param description:       input for the description of the new project
     * @param developersWorking: the number of developers working on this project, helps in estimating
     * @param teamVelocity:      the velocity of the team from previous projects
     * @param sprintDuration     : the duration of each sprint in this project
     * @param redir              :allows the controller to add 'flash' attributes, which will only be valid during redirection
     * @param session:           the current session, needed to find the creator of the project
     * @return : returns a redirection to the project panel
     * @throws ProjectAlreadyExistsException : if the user tries to create a project with a title of another project that already
     *                                       exists
     * @throws ProjectHasEmptyTitleException : the user cannot create a project with no title
     *                                       working and estimated sprints needed
     */
    @PostMapping("/createProject")
    public String createProject(@RequestParam String title,
                                @RequestParam String description,
                                @RequestParam String developersWorking,
                                @RequestParam String teamVelocity,
                                @RequestParam String sprintDuration,
                                RedirectAttributes redir,
                                HttpSession session) throws ProjectAlreadyExistsException, ProjectHasEmptyTitleException {
        projectService.createProject(title, description, developersWorking, teamVelocity, sprintDuration, sessionService.getUserWithSessionId(session));
        redir.addFlashAttribute("projectCreated", true);
        return "redirect:/user/projectPanel";
    }

    /**
     * this method calls the project service in order to update an existing project and store it in the repository
     *
     * @param projectId:                the project id in order to find it on the repository
     * @param projectTitle:             the possibly updated title of the project
     * @param projectDescription:       the possibly updated description of the project
     * @param projectDevelopersWorking: the number of developers that work on this project might change
     * @param projectSprintDuration:    update to duration of each sprint in the project
     * @param redir                     allows the controller to add 'flash' attributes, which will only be valid during redirection
     * @throws ProjectAlreadyExistsException : if the user tries to update a project with a title of another project that already
     *                                       exists
     * @throws ProjectHasEmptyTitleException : the user cannot update a project and sets its title to blank
     * @return: returns the project panel template (redirects)
     */
    @RequestMapping(value = "/editProject", params = "action=update", method = RequestMethod.POST)
    public String updateProject(@RequestParam long projectId,
                                @RequestParam String projectTitle,
                                @RequestParam String projectDescription,
                                @RequestParam String projectDevelopersWorking,
                                @RequestParam String projectSprintDuration,
                                RedirectAttributes redir) throws ProjectAlreadyExistsException, ProjectHasEmptyTitleException {
        projectService.updateProject(projectId, projectTitle, projectDescription, projectDevelopersWorking, projectSprintDuration);
        redir.addFlashAttribute("projectUpdated", true);
        return "redirect:/user/projectPanel";
    }

    /**
     * this method calls the project service in order to delete an existing project from the repository
     *
     * @param projectId: required id in order to delete the project from the repository
     * @param redir: allows the controller to add 'flash' attributes, which will only be valid during redirection
     * @return : returns project panel template (redirects)
     */
    @RequestMapping(value = "/editProject", params = "action=delete", method = RequestMethod.POST)
    public String deleteProject(@RequestParam long projectId,
                                RedirectAttributes redir) {
        projectService.deleteProject(projectId);
        redir.addFlashAttribute("projectDeleted", true);
        return "redirect:/user/projectPanel";
    }

    /**
     * this method calls the project service in order to create a new item and store it in the repository
     *
     * @param title:       input for the title of the new item
     * @param description: input for the description of the new item
     * @param acceptanceCriteria: under what conditions, is this item considered done
     * @param type:        input for the type of item
     * @param priority:    input for the priority of the item
     * @param effort:      input for the effort needed to complete the item
     * @param estimatedEffort    : input for the estimated effort needed to complete the item
     * @param projectId:   project that this item belongs to
     * @param assigneeId:  id of the assigned user
     * @param parentId     : the parent of the item
     * @param session:     current session, needed to find the creator of this item
     * @param redir: allows the controller to add 'flash' attributes, which will only be valid during redirection
     * @return : returns a redirection to the project page
     * @throws ItemAlreadyExistsException : user has tried to create an item with the same title
     * @throws ItemHasEmptyTitleException : user has tried to create an item with no title
     */
    @PostMapping("/createItem")
    public String createItem(@RequestParam String title,
                             @RequestParam String description,
                             @RequestParam String acceptanceCriteria,
                             @RequestParam String type,
                             @RequestParam String priority,
                             @RequestParam String effort,
                             @RequestParam String estimatedEffort,
                             @RequestParam long projectId,
                             @RequestParam long assigneeId,
                             @RequestParam long parentId,
                             HttpSession session,
                             RedirectAttributes redir) {
        try {
            itemService.createItem(title, description, acceptanceCriteria, ItemType.valueOf(type),
                    ItemPriority.valueOf(priority), effort, estimatedEffort, new Project(projectId),
                    new User(assigneeId), sessionService.getUserWithSessionId(session),
                    new Item(parentId));
            //this is also needed to toggle the 'Create an Item' form
            redir.addFlashAttribute("productBacklogItemCreated", true);
            //this is general use, used by all menus that can create items
            redir.addFlashAttribute("itemCreated", true);
        } catch (ItemAlreadyExistsException | ItemHasEmptyTitleException e) {
            redir.addFlashAttribute("createdProductBacklogItemTitle", title);
            redir.addFlashAttribute("createdProductBacklogItemDescription", description);
            redir.addFlashAttribute("createdProductBacklogItemAcceptanceCriteria", acceptanceCriteria);
            //received full capitalized from front-end so we need to reformat it to first letter only capital
            String reformattedTypeStr = type.substring(0, 1) + type.substring(1).toLowerCase();
            redir.addFlashAttribute("createdProductBacklogItemType", reformattedTypeStr);
            //same as above
            String reformattedPriorityStr = priority.substring(0, 1) + priority.substring(1).toLowerCase();
            redir.addFlashAttribute("createdProductBacklogItemPriority", reformattedPriorityStr);
            redir.addFlashAttribute("createdProductBacklogItemParentId", parentId);
            if (e instanceof ItemAlreadyExistsException) {
                redir.addFlashAttribute("createdProductBacklogItemAlreadyExists", true);
            } else {
                redir.addFlashAttribute("createdProductBacklogItemHasEmptyTitle", true);
            }
        }
        return "redirect:/user/project/" + projectId + "#createItemForm";
    }
}

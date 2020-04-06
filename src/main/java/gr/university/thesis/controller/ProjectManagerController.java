package gr.university.thesis.controller;

import gr.university.thesis.entity.Item;
import gr.university.thesis.entity.Project;
import gr.university.thesis.entity.Sprint;
import gr.university.thesis.entity.User;
import gr.university.thesis.entity.enumeration.ItemPriority;
import gr.university.thesis.entity.enumeration.ItemType;
import gr.university.thesis.exceptions.*;
import gr.university.thesis.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.Optional;

/**
 * This is the project manager controller, here, a project manager can request actions such as viewing all projects
 * or creating one
 */
@Controller
@RequestMapping("/pm")
public class ProjectManagerController {

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
    public ProjectManagerController(ProjectService projectService, SessionService sessionService,
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
                                HttpSession session) throws ProjectAlreadyExistsException, ProjectHasEmptyTitleException {
        projectService.createProject(title, description, developersWorking, teamVelocity, sprintDuration, sessionService.getUserWithSessionId(session));
        return "redirect:/user/projectPanel";
    }

    /**
     * this method calls the project service in order to update an existing project and store it in the repository
     *
     * @param projectId:                     the project id in order to find it on the repository
     * @param projectTitle:                  the possibly updated title of the project
     * @param projectDescription:            the possibly updated description of the project
     * @param projectDevelopersWorking:      the number of developers that work on this project might change
     * @param projectSprintDuration:         update to duration of each sprint in the project
     * @throws ProjectAlreadyExistsException : if the user tries to update a project with a title of another project that already
     *                                       exists
     * @throws ProjectHasEmptyTitleException : the user cannot update a project and sets its title to blank
     */
    @RequestMapping(value = "/editProject", params = "action=update", method = RequestMethod.POST)
    public String updateProject(@RequestParam long projectId,
                                @RequestParam String projectTitle,
                                @RequestParam String projectDescription,
                                @RequestParam String projectDevelopersWorking,
                                @RequestParam String projectSprintDuration) throws ProjectAlreadyExistsException, ProjectHasEmptyTitleException {
        projectService.updateProject(projectId, projectTitle, projectDescription, projectDevelopersWorking, projectSprintDuration);
        return "redirect:/user/projectPanel";
    }

    /**
     * this method calls the project service in order to delete an existing project from the repository
     *
     * @param projectId: required id in order to delete the project from the repository
     * @return : returns project panel template (redirects)
     */
    @RequestMapping(value = "/editProject", params = "action=delete", method = RequestMethod.POST)
    public String deleteProject(@RequestParam long projectId) {
        projectService.deleteProject(projectId);
        return "redirect:/user/projectPanel";
    }

    /**
     * this method calls the project service in order to create a new item and store it in the repository
     *
     * @param title:       input for the title of the new project
     * @param description: input for the description of the new project
     * @param acceptanceCriteria: under what conditions, is this item considered done
     * @param type:        input for the type of item
     * @param priority:    input for the priority of the item
     * @param effort:      input for the effort needed to complete the item
     * @param estimatedEffort    : input for the estimated effort needed to complete the item
     * @param projectId:   project that this item belongs to
     * @param assigneeId:  id of the assigned user
     * @param parentId     : the parent of the item
     * @param session:     current session, needed to find the creator of this item
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
                             HttpSession session) throws ItemAlreadyExistsException, ItemHasEmptyTitleException {
        itemService.createItem(title, description, acceptanceCriteria, ItemType.valueOf(type),
                ItemPriority.valueOf(priority), effort, estimatedEffort, new Project(projectId),
                new User(assigneeId), sessionService.getUserWithSessionId(session),
                new Item(parentId));
        return "redirect:/user/project/" + projectId;
    }

    /**
     * this method calls the item service in order to update an existing item and store it in the repository
     *
     * @param itemProjectId:   the id of the project, needed for the redirection
     * @param itemId:          id of the item, needed to find it on the repository
     * @param itemTitle:       title of the item
     * @param itemDescription: description of the item
     * @param itemAcceptanceCriteria: under what conditions, is this item considered done
     * @param itemType:        ItemType of the item
     * @param itemPriority:    ItemPriority of the item
     * @param itemEffort:      effort required to finish this item
     * @param itemEstimatedEffort: estimated effort required to finish this item
     * @param itemAssigneeId:  the user that thisitem is assigned to
     * @param itemParentId    : the parent of the item
     * @param sprintId : the sprint that this item belongs to
     * @param modifyItemPage: allows the controller to know, which page this request comes from
     * @param redir: allows the controller to add 'flash' attributes, which will only be valid during redirection
     * @return : returns a redirection to the current project backlog
     * @throws ItemAlreadyExistsException : user has tried to set the item's title to one that already exists
     * @throws ItemHasEmptyTitleException : user has tried to set the item's title to blank
     */
    @RequestMapping(value = "/editItem", params = "action=update", method = RequestMethod.POST)
    public String updateItem(@RequestParam long itemProjectId,
                             @RequestParam long itemId,
                             @RequestParam String itemTitle,
                             @RequestParam String itemDescription,
                             @RequestParam String itemAcceptanceCriteria,
                             @RequestParam String itemType,
                             @RequestParam String itemPriority,
                             @RequestParam String itemEffort,
                             @RequestParam String itemEstimatedEffort,
                             @RequestParam long itemAssigneeId,
                             @RequestParam long itemParentId,
                             @RequestParam long sprintId,
                             @RequestParam String modifyItemPage,
                             RedirectAttributes redir
    ) throws ItemAlreadyExistsException, ItemHasEmptyTitleException {
        //the associations need to be defined before the item is updated
        itemSprintHistoryService.manageItemSprintAssociation(new Item(itemId), new Sprint(sprintId), new Item(itemParentId));
        itemService.updateItem(itemId, itemTitle, itemDescription, itemAcceptanceCriteria, itemType, itemPriority,
                itemEffort, itemEstimatedEffort,
                new User(itemAssigneeId), new Item(itemParentId));
        if (modifyItemPage.equals("projectPage"))
            return "redirect:/user/project/" + itemProjectId;
        else {
            redir.addFlashAttribute("itemId", itemId);
            switch (modifyItemPage) {
                case "viewItem":
                    return "redirect:/user/project/" + itemProjectId;
                case "projectProgressPage":
                    return "redirect:/user/project/" + itemProjectId + "/projectProgress";
                case "taskBoardPage":
                    return "redirect:/user/project/" + itemProjectId + "/sprint/" + sprintId;
                case "sprintHistoryPage":
                    return "redirect:/user/project/" + itemProjectId + "/sprint/" + sprintId + "/history";
            }
        }
        return "redirect:/";
    }

    /**
     * this method calls the item service in order to delete an existing item from the repository
     *
     * @param itemId:         required id in order to delete the item from the repository
     * @param itemProjectId:  required for the redirection to the project backlog
     * @param sprintId        : the sprint that this item belongs to
     * @param modifyItemPage: allows the controller to know, which page this request comes from
     * @param redir:          allows the controller to add 'flash' attributes, which will only be valid during redirection
     * @return : returns a redirection to the current project backlog
     */
    @RequestMapping(value = "/editItem", params = "action=delete", method = RequestMethod.POST)
    public String deleteItem(@RequestParam long itemId,
                             @RequestParam long itemProjectId,
                             @RequestParam long sprintId,
                             @RequestParam String modifyItemPage,
                             RedirectAttributes redir) {
        //need to delete its associations first (if they exist)
        itemSprintHistoryService.removeItemFromSprint(new Item(itemId), new Sprint(sprintId), null);
        itemService.deleteItem(itemId);
        if (modifyItemPage.equals("projectPage"))
            return "redirect:/user/project/" + itemProjectId;
        else {
            redir.addFlashAttribute("itemId", -1);
            switch (modifyItemPage) {
                case "viewItem":
                    return "redirect:/user/project/" + itemProjectId;
                case "projectProgressPage":
                    return "redirect:/user/project/" + itemProjectId + "/projectProgress";
                case "taskBoardPage":
                    return "redirect:/user/project/" + itemProjectId + "/sprint/" + sprintId;
                case "sprintHistoryPage":
                    return "redirect:/user/project/" + itemProjectId + "/sprint/" + sprintId + "/history";
            }
        }
        return "redirect:/";
    }

    /**
     * this method calls the ItemSprintHistory service in order to move an item to a ready sprint
     *
     * @param itemId:        required id in order to know which item to move to the sprint
     * @param sprintId:      require id in order to know to which sprint it should move the item to
     * @param itemProjectId: required for the redirection to the project backlog
     * @return : returns a redirection to the current project backlog
     */
    @RequestMapping(value = "/editItem", params = "action=move", method = RequestMethod.POST)
    public String moveItemToSprint(@RequestParam long itemId,
                                   @RequestParam long sprintId,
                                   @RequestParam long itemProjectId) {
        itemSprintHistoryService.moveItemToSprint(new Item(itemId), new Sprint(sprintId), null);
        return "redirect:/user/project/" + itemProjectId;
    }

    /**
     * this method calls the ItemSprintHistory service in order to remove an item to a ready sprint
     *
     * @param itemId:        required id in order to know which item to move to the sprint
     * @param sprintId:      require id in order to know to which sprint it should move the item to
     * @param itemProjectId: required for the redirection to the project backlog
     * @return : returns a redirection to the current project backlog
     */
    @RequestMapping(value = "/editItem", params = "action=remove", method = RequestMethod.POST)
    public String removeItemFromSprint(@RequestParam long itemId,
                                       @RequestParam long sprintId,
                                       @RequestParam long itemProjectId) {
        System.out.println(itemId + " " + sprintId);
        itemSprintHistoryService.removeItemFromSprint(new Item(itemId), new Sprint(sprintId), null);
        return "redirect:/user/project/" + itemProjectId;
    }

    /**
     * this method moves a ready sprint to the state of active
     *
     * @param sprintId:        the sprint that the user wants to start
     * @param sprintProjectId: the project that this sprint belongs to
     * @param sprintGoal       : the goal the users are trying to achieve by the end of this sprint
     * @return : redirection to project page
     * @throws SprintHasZeroEffortException : this exception is thrown when there are no tasks/bugs in the sprint, and the user
     *                                      is trying to start it
     */
    @RequestMapping(value = "/editSprint", params = "action=start", method = RequestMethod.POST)
    public String startSprint(@RequestParam long sprintId,
                              @RequestParam long sprintProjectId,
                              @RequestParam String sprintGoal) throws SprintHasZeroEffortException {
        sprintService.startSprint(sprintId, sprintGoal);
        return "redirect:/user/project/" + sprintProjectId;
    }

    /**
     * this methods finishes a sprint and moves it to a finish state, and creates a new one for the current project,
     * it also transfers all unfinished items to the next sprint
     *
     * @param sprintId:        the sprint that the user wants to finish
     * @param sprintProjectId: the project that this sprint belongs to
     * @return : redirection to project page
     */
    @RequestMapping(value = "/editSprint", params = "action=finish", method = RequestMethod.POST)
    public String finishSprint(@RequestParam long sprintId,
                               @RequestParam long sprintProjectId) {
        Optional<Sprint> oldSprintOptional = sprintService.finishSprint(sprintId);
        if (oldSprintOptional.isPresent()) {
            Sprint sprint = oldSprintOptional.get();
            sprintService.calculateVelocity(sprint);
            projectService.findProjectById(sprint.getProject().getId()).ifPresent(project -> project.setTeam_velocity(sprint.getVelocity()));
        }
        oldSprintOptional.ifPresent(sprint -> itemSprintHistoryService.transferUnfinishedItemsFromOldSprint(sprint));
        return "redirect:/user/project/" + sprintProjectId;
    }
}

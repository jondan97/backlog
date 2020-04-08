package gr.university.thesis.controller;

import gr.university.thesis.entity.Item;
import gr.university.thesis.entity.Sprint;
import gr.university.thesis.exceptions.SprintHasZeroEffortException;
import gr.university.thesis.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

/**
 * This is the scrum master controller, here, a scrum master can request actions such as starting a sprint or moving
 * an item to the sprint
 */
@Controller
@RequestMapping("/sm")
public class ScrumMasterController {

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
    public ScrumMasterController(ProjectService projectService, SessionService sessionService,
                                 ItemService itemService, SprintService sprintService,
                                 ItemSprintHistoryService itemSprintHistoryService) {
        this.projectService = projectService;
        this.sessionService = sessionService;
        this.itemService = itemService;
        this.sprintService = sprintService;
        this.itemSprintHistoryService = itemSprintHistoryService;
    }

    /**
     * this method calls the ItemSprintHistory service in order to move an item to a ready sprint
     *
     * @param itemId:        required id in order to know which item to move to the sprint
     * @param sprintId:      require id in order to know to which sprint it should move the item to
     * @param itemProjectId: required for the redirection to the project backlog
     * @return : returns a redirection to the current project backlog
     */
    @RequestMapping(value = "/moveItem", params = "action=move", method = RequestMethod.POST)
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
    @RequestMapping(value = "/moveItem", params = "action=remove", method = RequestMethod.POST)
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

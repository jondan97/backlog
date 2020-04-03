package gr.university.thesis.controller;


import gr.university.thesis.dto.BurnDownChartData;
import gr.university.thesis.entity.*;
import gr.university.thesis.entity.enumeration.ItemPriority;
import gr.university.thesis.entity.enumeration.ItemType;
import gr.university.thesis.entity.enumeration.SprintStatus;
import gr.university.thesis.entity.enumeration.TaskBoardStatus;
import gr.university.thesis.exceptions.ItemDoesNotExistException;
import gr.university.thesis.exceptions.ProjectDoesNotExistException;
import gr.university.thesis.exceptions.SprintDoesNotExistException;
import gr.university.thesis.exceptions.SprintHasNotStartedException;
import gr.university.thesis.service.*;
import gr.university.thesis.util.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import java.util.*;

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
    SprintService sprintService;
    ItemSprintHistoryService itemSprintHistoryService;

    /**
     * constructor of this class, correct way to set the autowired attributes
     *
     * @param projectService:           service that manages all the projects of the system
     * @param sessionService:           the service that manages the current session
     * @param userService:              service that manages all the users of the system
     * @param itemService:              service that handles all the items of the system
     * @param commentService:           service that handles all the comments
     * @param sprintService:            service that handles all the sprints
     * @param itemSprintHistoryService: service that handles all associations between items and sprints
     */
    @Autowired
    public UserController(ProjectService projectService, SessionService sessionService, UserService userService,
                          ItemService itemService, CommentService commentService, SprintService sprintService,
                          ItemSprintHistoryService itemSprintHistoryService) {
        this.projectService = projectService;
        this.sessionService = sessionService;
        this.userService = userService;
        this.itemService = itemService;
        this.commentService = commentService;
        this.sprintService = sprintService;
        this.itemSprintHistoryService = itemSprintHistoryService;
    }

    /**
     * 'main' page of user, here, the user(s) can manage all the project of the system
     *
     * @param model:   user interface that is shown to user
     * @param session: session required to get the current's admin ID (only the admin can access this method so we
     *                 find the session's userId attribute
     * @return : returns the manageUsers template
     */
    @GetMapping("/projectPanel")
    public String viewProjectsPanel(Model model, HttpSession session) {
        List<Project> allProjects = projectService.findAllProjects();
        model.addAttribute("projects", allProjects);
        return "projectPanel";
    }

    /**
     * this method shows to the user, the backlog and the sprints of a certain project
     *
     * @param projectId: the project id that was requested by the user to view
     * @param model:     the user interface that will be shown in the front-end
     * @return : returns the project template
     * @throws ProjectDoesNotExistException: throws this exception when the user tries to access a project that does
     *                                       not exist
     */
    @RequestMapping(value = "/project/{projectId}")
    public String viewProject(@PathVariable long projectId,
                              Model model) throws ProjectDoesNotExistException {
        Optional<Project> projectOptional = projectService.findProjectById(projectId);
        if (projectOptional.isPresent()) {
            Project project = projectOptional.get();
            model.addAttribute("project", project);
            Optional<Sprint> sprintOptional = sprintService.findActiveSprintInProject(project);
            Sprint sprint;
            if (sprintOptional.isPresent()) {
                sprint = sprintOptional.get();
                //this checks if the difference in ms remaining are 0 or less, which means that the sprint duration has expired
                //and the sprint needs to be set to finished
                if (sprint.getStatus() == SprintStatus.ACTIVE.getRepositoryId()
                        && Time.calculateDifferenceInMs(new Date(), sprint.getEnd_date()) <= 0) {
                    sprintService.finishSprint(sprint.getId());
                    itemSprintHistoryService.transferUnfinishedItemsFromOldSprint(sprint);
                    sprint = sprintService.findActiveSprintInProject(project).get();
                }
                model.addAttribute("sprint", sprint);
            }
            //perhaps not the most sufficient but this application is not supposed to be scalable
            model.addAttribute("allUsers", userService.findAllUsers());
            model.addAttribute("backlog", itemService.findAllItemsByProjectId(projectId));
            model.addAttribute("itemTypes", ItemType.values());
            model.addAttribute("itemPriorities", ItemPriority.values());
        } else {
            throw new ProjectDoesNotExistException("Project with id '" + projectId + "' does not exist.");
        }
        return "project";
    }

    /**
     * this method shows to the user, the backlog and the sprints of a certain project
     *
     * @param projectId:     the project id that was requested by the user to view
     * @param itemId         : the id of the item that the user requested to see
     * @param modalView      : this allows the controller to know, whether the request comes from a modal or not
     * @param modalSource:   this allows the controller to know, what is the source of the modal, is it accessed in the
     *                       project page, the task board page etc.
     * @param sprintIdModal: this helps the controller acquire the sprint id, by accessing the GET request
     *                       *                            parameter
     * @param model:         the user interface that will be shown in the front-end
     * @param redir:         allows the controller to add 'flash' attributes, which will only be valid during redirection
     * @return : returns the item template
     * @throws ItemDoesNotExistException : exception thrown when item does not exist in the project
     */
    @RequestMapping(value = "/project/{projectId}/item/{itemId}")
    public String viewItem(@PathVariable long projectId,
                           @PathVariable long itemId,
                           @RequestParam(required = false) boolean modalView,
                           @RequestParam(required = false) Optional<String> modalSource,
                           @RequestParam(required = false) String sprintIdModal,
                           Model model,
                           RedirectAttributes redir) throws ItemDoesNotExistException {
        Optional<Item> itemOptional = itemService.findItemInProject(itemId, projectId);
        Optional<Project> projectOptional = projectService.findProjectById(projectId);
        if (itemOptional.isPresent()) {
            Item item = itemOptional.get();
            boolean itemHasFinishedChildren = false;
            if (item.getType() == ItemType.EPIC.getRepositoryId() || item.getType() == ItemType.STORY.getRepositoryId()) {
                List<Item> temporaryOneSizedArrayList = new ArrayList<>();
                temporaryOneSizedArrayList.add(item);
                itemService.calculatedCombinedEffort(temporaryOneSizedArrayList);
                itemHasFinishedChildren = itemService.checkIfItemContainsFinishedChildren(item);
            }
            Project project = projectOptional.get();
            //if the user tries to access a sprint from the history page, then on update or whatever, they are redirected
            //to finished sprint and not active
            if (sprintIdModal != null) {
                long sprintId = Long.parseLong(sprintIdModal);
                model.addAttribute("sprint", sprintService.findSprintById(new Sprint(sprintId)).get());
            } else {
                model.addAttribute("sprint", sprintService.findActiveSprintInProject(project).get());
            }
            model.addAttribute("project", project);
            //perhaps not the most sufficient but this application is not supposed to be scalable
            model.addAttribute("allUsers", userService.findAllUsers());
            model.addAttribute("backlog", itemService.findAllItemsByProjectId(projectId));

            model.addAttribute("itemTypes", ItemType.values());
            model.addAttribute("itemPriorities", ItemPriority.values());
            //we want the most recent one shown first, we sort our item comments using a comparator, and then we reverse it
            Collections.sort(item.getComments(), Collections.reverseOrder(Comparator.comparing(Comment::getDate_created)));
            model.addAttribute("item", item);
            model.addAttribute("itemHasFinishedChildren", itemHasFinishedChildren);
        } else {
            throw new ItemDoesNotExistException("Item with id '" + itemId + "' does not exist in this project.");
        }
        //if the user asks to view a parent item through the modal view
        if (modalView && modalSource.isPresent()) {
            redir.addFlashAttribute("itemId", itemId);
            if (modalSource.get().equals("projectPage")) {
                return "redirect:/user/project/" + projectId;
            } else if (modalSource.get().equals("projectProgressPage")) {
                return "redirect:/user/project/" + projectId + "/projectProgress";
            } else if (modalSource.get().equals("taskBoardPage")) {
                return "redirect:/user/project/" + projectId + "/sprint/" + sprintIdModal;
            } else if (modalSource.get().equals("sprintHistoryPage")) {
                return "redirect:/user/project/" + projectId + "/sprint/" + sprintIdModal + "/history";
            }
        }
        return "item";
    }

    /**
     * this method updates the assignee of an item to another assignee, and only admins/projects managers or current
     * assignees can change that
     *
     * @param itemId:         the id of the item that we want the new assignee to 'own'
     * @param itemAssigneeId: the new assignee
     * @param itemProjectId:  the project that this item belongs to
     * @param sprintId             : the sprint that this assignee belongs to
     * @param updateAssigneeButton : the source that this request comes from, is it from the project page or the
     *                             task board page
     * @param redir: allows the controller to add 'flash' attributes, which will only be valid during redirection
     * @return : redirection to project page if assignment was done in project page, taskBoard page if assignment
     * was done in taskBoard page or home page if assignment failed to be recognised where it came from
     */
    @RequestMapping(value = "/updateAssignee")
    public String updateAssignee(@RequestParam long itemId,
                                 @RequestParam long itemAssigneeId,
                                 @RequestParam long itemProjectId,
                                 @RequestParam long sprintId,
                                 @RequestParam String updateAssigneeButton,
                                 RedirectAttributes redir) {
        itemService.updateAssignee(itemId, new User(itemAssigneeId));
        if (updateAssigneeButton.equals("projectPage"))
            return "redirect:/user/project/" + itemProjectId;
        else if (updateAssigneeButton.equals("taskBoard"))
            return "redirect:/user/project/" + itemProjectId + "/sprint/" + sprintId;
        else {
            redir.addFlashAttribute("itemId", itemId);
            switch (updateAssigneeButton) {
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
     * this method calls the comment service in order to create a new comment and store it in the repository
     *
     * @param commentBody:   body of the comment, what is says
     * @param commentItemId: item that this belongs to
     * @param commentProjectId : the project that this comment belongs to
     * @param sprintIdModalCommentCreate: this helps the controller acquire the sprint id, by accessing the GET request
     *                            parameter
     * @param modifyItemPageCommentCreate: allows the controller to know, which page this request comes from
     * @param redir: allows the controller to add 'flash' attributes, which will only be valid during redirection
     * @param session:       the current session, needed to find the creator of the comment
     * @return : returns a redirection to the item of a project
     */
    @PostMapping("/createComment")
    public String createComment(@RequestParam String commentBody,
                                @RequestParam long commentItemId,
                                @RequestParam long commentProjectId,
                                @RequestParam String modifyItemPageCommentCreate,
                                @RequestParam(required = false) Optional<Long> sprintIdModalCommentCreate,
                                HttpSession session,
                                RedirectAttributes redir) {
        commentService.createComment(commentBody, new Item(commentItemId), sessionService.getUserWithSessionId(session));
        redir.addFlashAttribute("itemId", commentItemId);
        switch (modifyItemPageCommentCreate) {
            case "viewItem":
                return "redirect:/user/project/" + commentProjectId;
            case "projectProgressPage":
                return "redirect:/user/project/" + commentProjectId + "/projectProgress";
            case "taskBoardPage":
                return "redirect:/user/project/" + commentProjectId + "/sprint/" + sprintIdModalCommentCreate.get();
            case "sprintHistoryPage":
                return "redirect:/user/project/" + commentProjectId + "/sprint/" + sprintIdModalCommentCreate.get() + "/history";
        }
        return "redirect:/";
    }

    /**
     * this method calls the comment service in order to update an existing comment and store it in the repository
     *
     * @param commentIdView:     the comment id in order to find it on the repository
     * @param commentItemIdView: required for the redirection
     * @param commentProjectIdView : required for the redirection
     * @param commentBodyView:   body of the comment, what is says
     * @param sprintIdModalComment: this helps the controller acquire the sprint id, by accessing the GET request
     *                            parameter
     * @param modifyItemPageComment: allows the controller to know, which page this request comes from
     * @param redir: allows the controller to add 'flash' attributes, which will only be valid during redirection
     * @return : returns a redirection to the item of a project
     */
    @RequestMapping(value = "/editComment", params = "action=update", method = RequestMethod.POST)
    public String updateComment(@RequestParam long commentIdView,
                                @RequestParam long commentItemIdView,
                                @RequestParam long commentProjectIdView,
                                @RequestParam String commentBodyView,
                                @RequestParam String modifyItemPageComment,
                                @RequestParam(required = false) Optional<Long> sprintIdModalComment,
                                RedirectAttributes redir) {
        commentService.updateComment(commentIdView, commentBodyView);
        redir.addFlashAttribute("itemId", commentItemIdView);
        switch (modifyItemPageComment) {
            case "viewItem":
                return "redirect:/user/project/" + commentProjectIdView;
            case "projectProgressPage":
                return "redirect:/user/project/" + commentProjectIdView + "/projectProgress";
            case "taskBoardPage":
                return "redirect:/user/project/" + commentProjectIdView + "/sprint/" + sprintIdModalComment.get();
            case "sprintHistoryPage":
                return "redirect:/user/project/" + commentProjectIdView + "/sprint/" + sprintIdModalComment.get() + "/history";
        }
        return "redirect:/";
    }

    /**
     * this method calls the comment service in order to delete an existing comment from the repository
     *
     * @param commentIdView:        required id in order to delete the comment from the repository
     * @param commentItemIdView:    required for the redirection
     * @param commentProjectIdView: required for the redirection
     * @param sprintIdModalComment: this helps the controller acquire the sprint id, by accessing the GET request
     *                            parameter
     * @param modifyItemPageComment: allows the controller to know, which page this request comes from
     * @param redir: allows the controller to add 'flash' attributes, which will only be valid during redirection
     * @return : returns a redirection to the item of a project
     */
    @RequestMapping(value = "/editComment", params = "action=delete", method = RequestMethod.POST)
    public String deleteComment(@RequestParam long commentIdView,
                                @RequestParam long commentItemIdView,
                                @RequestParam long commentProjectIdView,
                                @RequestParam String modifyItemPageComment,
                                @RequestParam(required = false) Optional<Long> sprintIdModalComment,
                                RedirectAttributes redir
    ) {
        commentService.deleteComment(commentIdView);
        redir.addFlashAttribute("itemId", commentItemIdView);
        switch (modifyItemPageComment) {
            case "viewItem":
                return "redirect:/user/project/" + commentProjectIdView;
            case "projectProgressPage":
                return "redirect:/user/project/" + commentProjectIdView + "/projectProgress";
            case "taskBoardPage":
                return "redirect:/user/project/" + commentProjectIdView + "/sprint/" + sprintIdModalComment.get();
            case "sprintHistoryPage":
                return "redirect:/user/project/" + commentProjectIdView + "/sprint/" + sprintIdModalComment.get() + "/history";
        }
        return "redirect:/";
    }

    /**
     * this method shows to the user, the task board of a sprint that belongs to a certain project
     *
     * @param projectId: the project that this sprint belongs to
     * @param sprintId:  the sprint that contains the tasks the user has requested to see
     * @param model:     the user interface
     * @return : the task board of the sprint requested
     * @throws SprintDoesNotExistException: this is thrown when the sprint requested does not exist in this project
     * @throws SprintHasNotStartedException: this is thrown when the user tries to access the task board of a ready
     * sprint or a task board that in general, is not in the active state
     */
    @RequestMapping(value = "/project/{projectId}/sprint/{sprintId}")
    public String viewTaskBoard(@PathVariable long projectId,
                                @PathVariable long sprintId,
                                Model model) throws SprintDoesNotExistException, SprintHasNotStartedException {
        Optional<Sprint> sprintOptional = projectService.findSprintInProject(projectId, sprintId);
        if (sprintOptional.isPresent()) {
            Sprint sprint = sprintOptional.get();
            if (sprint.getStatus() == SprintStatus.READY.getRepositoryId()) {
                throw new SprintHasNotStartedException("Sprint task board has not yet started.");
            }
            model.addAttribute("allUsers", userService.findAllUsers());
            model.addAttribute("projectId", projectId);
            model.addAttribute("sprint", sprint);
            Optional<List<ItemSprintHistory>> todoAssociations = itemSprintHistoryService.
                    findAllAssociationsByStatus(new Sprint(sprintId), TaskBoardStatus.TO_DO,
                            ItemType.TASK, ItemType.BUG);
            todoAssociations.ifPresent(itemSprintHistories -> model.addAttribute("todoAssociations",
                    itemSprintHistories));
            Optional<List<ItemSprintHistory>> inProgressAssociations = itemSprintHistoryService.
                    findAllAssociationsByStatus(new Sprint(sprintId), TaskBoardStatus.IN_PROGRESS,
                            ItemType.TASK, ItemType.BUG);
            inProgressAssociations.ifPresent(itemSprintHistories -> model.addAttribute("inProgressAssociations",
                    itemSprintHistories));
            Optional<List<ItemSprintHistory>> forReviewAssociations = itemSprintHistoryService.
                    findAllAssociationsByStatus(new Sprint(sprintId), TaskBoardStatus.FOR_REVIEW,
                            ItemType.TASK, ItemType.BUG);
            forReviewAssociations.ifPresent(itemSprintHistories -> model.addAttribute("forReviewAssociations",
                    itemSprintHistories));
            Optional<List<ItemSprintHistory>> doneAssociations = itemSprintHistoryService.
                    findAllAssociationsByStatus(new Sprint(sprintId), TaskBoardStatus.DONE,
                            ItemType.TASK, ItemType.BUG);
            doneAssociations.ifPresent(itemSprintHistories -> model.addAttribute("doneAssociations",
                    itemSprintHistories));
            model.addAttribute("itemPriorities", ItemPriority.values());
        } else {
            throw new SprintDoesNotExistException("Sprint with id '" + sprintId + "' does not exist in this project");
        }
        return "taskboard";
    }

    /**
     * this method calls the itemSprintHistory service in order to move an association
     * to the next status and store it in the repository
     *
     * @param sprintId:      the sprint id of the association
     * @param itemId:        the item id of the assoication
     * @param itemProjectId: the project id that this association belongs to
     * @return : returns a redirection to the task board page of the sprint
     */
    @RequestMapping(value = "/editAssociation", params = "action=nextStatus", method = RequestMethod.POST)
    public String moveAssociationStatusToNext(@RequestParam long sprintId,
                                              @RequestParam long itemId,
                                              @RequestParam long itemProjectId) {
        itemSprintHistoryService.changeStatusOfAssociationByOne(new Sprint(sprintId), new Item(itemId), 1);
        return "redirect:/user/project/" + itemProjectId + "/sprint/" + sprintId;
    }

    /**
     * this method calls the itemSprintHistory service in order to move an association
     * to the previous status and store it in the repository
     *
     * @param sprintId:      the sprint id of the association
     * @param itemId:        the item id of the assoication
     * @param itemProjectId: the project id that this association belongs to
     * @return : returns a redirection to the task board page of the sprint
     */
    @RequestMapping(value = "/editAssociation", params = "action=previousStatus", method = RequestMethod.POST)
    public String moveAssociationStatusToPrevious(@RequestParam long sprintId,
                                                  @RequestParam long itemId,
                                                  @RequestParam long itemProjectId) {
        itemSprintHistoryService.changeStatusOfAssociationByOne(new Sprint(sprintId), new Item(itemId), -1);
        return "redirect:/user/project/" + itemProjectId + "/sprint/" + sprintId;
    }

    /**
     * this method shows to the user the progress of a project, which includes all the sprints that took place and
     * a burn down chart for all the project
     *
     * @param projectId: the project that the user requested to see the history of
     * @param model:     the interface that the user sees
     * @return : returns the projectHistory template
     * @throws ProjectDoesNotExistException: this is thrown when a user tries to access the history of a
     *                                       project that does not exist
     */
    @RequestMapping(value = "/project/{projectId}/projectProgress")
    public String viewProjectProgress(@PathVariable long projectId,
                                      Model model) throws ProjectDoesNotExistException {
        if (projectService.findProjectById(projectId).isPresent()) {
            Optional<List<Sprint>> finishedSprintsOptional =
                    sprintService.findSprintsByProjectAndStatus(new Project(projectId), SprintStatus.FINISHED);
            BurnDownChartData burnDownChartData;
            if (finishedSprintsOptional.isPresent()) {
                //thymeleaf doesn't recognise 'List' so we have to move it to an ArrayList
                ArrayList<Sprint> finishedSprints = (ArrayList<Sprint>) finishedSprintsOptional.get();
                model.addAttribute("finishedSprints", finishedSprints);
                //burn down chart data are needed to be shown  for project total effort and current sprint if it's running
                burnDownChartData = projectService.calculateBurnDownChartData(projectId, finishedSprints);
            } else {
                //burn down chart data are needed to be shown  for project total effort and current sprint if it's running
                burnDownChartData = projectService.calculateBurnDownChartData(projectId, null);
            }
            model.addAttribute("burnDownChartData", burnDownChartData);
        } else {
            throw new ProjectDoesNotExistException("Project with id '" + projectId + "' does not exist.");
        }

        return "projectProgress";
    }

    /**
     * this method shows to the user the history of a sprint, along with all the done tasks (per day), and a burn down
     * chart
     *
     * @param projectId: the project that this sprint belongs to
     * @param sprintId:  the sprint that the user requested to see the history of
     * @param model      : interface that will be shown to the user
     * @return : returns the sprintHistory template
     * @throws SprintDoesNotExistException: this is thrown when the sprint that the user is trying to access its history
     *                                      of, does not exist
     */
    @RequestMapping(value = "/project/{projectId}/sprint/{sprintId}/history")
    public String sprintHistory(@PathVariable long projectId,
                                @PathVariable long sprintId,
                                Model model) throws SprintDoesNotExistException {
        Optional<Sprint> sprintOptional =
                sprintService.findSprintByProjectId(projectId, sprintId);
        if (sprintOptional.isPresent()) {
            Sprint sprint = sprintOptional.get();
            model.addAttribute("tasksDoneByDateList", itemSprintHistoryService.sortTasksByDate(sprint));
            model.addAttribute("burnDownChartData", itemSprintHistoryService.calculateBurnDownChartData(sprint));
        } else {
            throw new SprintDoesNotExistException("Sprint with id '" + sprintId + "' does not exist in this project.");
        }
        return "sprintHistory";
    }

}

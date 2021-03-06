package gr.university.thesis.controller;


import gr.university.thesis.dto.BurnDownChartData;
import gr.university.thesis.entity.*;
import gr.university.thesis.entity.enumeration.*;
import gr.university.thesis.exceptions.*;
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
     * @param model: user interface that is shown to user
     * @return : returns the manageUsers template
     */
    @GetMapping("/projectPanel")
    public String viewProjectsPanel(Model model) {
        List<Project> allProjects = projectService.findAllProjects();
        model.addAttribute("projects", allProjects);
        return "projectPanel";
    }

    /**
     * this method shows to the user, the backlog and the sprints of a certain project
     *
     * @param projectId: the project id that was requested by the user to view
     * @param model:     the user interface that will be shown in the front-end
     * @param redir:     allows the controller to add 'flash' attributes, which will only be valid during redirection
     * @return : returns the project template
     * @throws ProjectDoesNotExistException: throws this exception when the user tries to access a project that does
     *                                       not exist
     */
    @RequestMapping(value = "/project/{projectId}")
    public String viewProject(@PathVariable long projectId,
                              Model model,
                              RedirectAttributes redir) throws ProjectDoesNotExistException {
        Optional<Project> projectOptional = projectService.findProjectById(projectId);
        if (projectOptional.isPresent()) {
            Project project = projectOptional.get();
            model.addAttribute("project", project);
            Iterable<Item> allItemsInProjectBacklog = itemService.findAllItemsInBacklogByProjectId(projectId, ItemStatus.BACKLOG);
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
                    redir.addFlashAttribute("sprintFinished", true);
                    redir.addFlashAttribute("previousSprintGoal", sprint.getGoal());
                    redir.addFlashAttribute("previousSprintVelocity", sprint.getVelocity());
                    redir.addFlashAttribute("previousSprintTotalEffort", sprint.getTotal_effort());
                    //need to redirect here because project throws null exception if returned as normal template
                    return "redirect:/user/project/" + projectId;
                }
                model.addAttribute("sprint", sprint);
                //for the navbar
                model.addAttribute("navSprint", sprint);
                boolean mostImportantItemsIncluded;
                if (sprint.getStatus() == SprintStatus.READY.getRepositoryId()) {
                    mostImportantItemsIncluded =
                            sprintService.findIfMostImportantItemsWereIncludedInSprint(sprint, allItemsInProjectBacklog);

                } else {
                    mostImportantItemsIncluded = true;
                }
                model.addAttribute("MostImportantItemsIncluded", mostImportantItemsIncluded);
            }
            //perhaps not the most sufficient but this application is not supposed to be scalable
            model.addAttribute("allUsers", userService.findAllUsers());
            model.addAttribute("backlog", allItemsInProjectBacklog);
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
        //the following was included because the developer doesn't want the item to be deleted if it has finished
        //children, as the finished children will be also deleted unless they turn to stray manually
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
     * @param itemId:               the id of the item that we want the new assignee to 'own'
     * @param itemAssigneeId:       the new assignee
     * @param itemProjectId:        the project that this item belongs to
     * @param sprintId              : the sprint that this assignee belongs to
     * @param modifyDeveloperButton : the source that this request comes from, is it from the project page or the
     *                              task board page
     * @param redir:                allows the controller to add 'flash' attributes, which will only be valid during redirection
     * @return : redirection to project page if assignment was done in project page, taskBoard page if assignment
     * was done in taskBoard page or home page if assignment failed to be recognised where it came from
     */
    @RequestMapping(value = "/modifyItemByDeveloper")
    public String modifyItemByDeveloper(@RequestParam long itemId,
                                        @RequestParam long itemAssigneeId,
                                        @RequestParam long itemProjectId,
                                        @RequestParam long sprintId,
                                        @RequestParam String modifyDeveloperButton,
                                        RedirectAttributes redir) {
        itemService.updateAssignee(itemId, new User(itemAssigneeId));
        if (modifyDeveloperButton.equals("projectPage"))
            return "redirect:/user/project/" + itemProjectId;
        else if (modifyDeveloperButton.equals("taskBoard"))
            return "redirect:/user/project/" + itemProjectId + "/sprint/" + sprintId;
        else {
            redir.addFlashAttribute("itemId", itemId);
            switch (modifyDeveloperButton) {
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
        Optional<Project> projectOptional = projectService.findProjectById(projectId);
        Optional<Sprint> sprintOptional = projectService.findSprintInProject(projectId, sprintId);
        Optional<Sprint> activeSprintOptional = sprintService.findActiveSprintInProject(new Project(projectId));
        activeSprintOptional.ifPresent(sprint -> model.addAttribute("navSprint", sprint));
        if (sprintOptional.isPresent()) {
            Project project = projectOptional.get();
            Sprint sprint = sprintOptional.get();
            model.addAttribute("project", project);
            model.addAttribute("sprint", sprint);
            if (sprint.getStatus() == SprintStatus.READY.getRepositoryId()) {
                throw new SprintHasNotStartedException("Sprint task board has not yet started.");
            }
            model.addAttribute("allUsers", userService.findAllUsers());
            model.addAttribute("projectId", projectId);
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
     * a burn down chart for all the projects
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
        Optional<Project> projectOptional = projectService.findProjectById(projectId);
        if (projectOptional.isPresent()) {
            Project project = projectOptional.get();
            model.addAttribute("project", project);
            Optional<Sprint> sprintOptional = sprintService.findActiveSprintInProject(project);
            sprintOptional.ifPresent(sprint -> model.addAttribute("sprint", sprint));
            //for the navbar
            sprintOptional.ifPresent(sprint -> model.addAttribute("navSprint", sprint));
            Optional<List<Sprint>> finishedSprintsOptional =
                    sprintService.findSprintsByProjectAndStatus(new Project(projectId), SprintStatus.FINISHED);
            BurnDownChartData burnDownChartData;
            if (finishedSprintsOptional.isPresent()) {
                //thymeleaf doesn't recognise 'List' so we have to move it to an ArrayList
                ArrayList<Sprint> finishedSprints = (ArrayList<Sprint>) finishedSprintsOptional.get();
                model.addAttribute("finishedSprints", finishedSprints);
                //burn down chart data are needed to be shown  for project total effort and current sprint if it's running
                burnDownChartData = projectService.calculateBurnDownChartData(projectId, finishedSprints);
                //else do not show any sprints, only the ideal burning (if it applies)
            } else {
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
            Optional<Project> projectOptional = projectService.findProjectById(projectId);
            Project project = projectOptional.get();
            model.addAttribute("project", projectOptional.get());
            Optional<Sprint> sprintActiveOptional = sprintService.findActiveSprintInProject(project);
            sprintActiveOptional.ifPresent(activeSprint -> model.addAttribute("navSprint", activeSprint));
            //for the navbar
            sprintOptional.ifPresent(activeSprint -> model.addAttribute("sprint", sprint));
        } else {
            throw new SprintDoesNotExistException("Sprint with id '" + sprintId + "' does not exist in this project.");
        }
        return "sprintHistory";
    }

    /**
     * this method calls the project service in order to create a new item in the current project
     * and store it in the repository. The user can click on a button that is right next to the parents that are shown
     * in the project and sprint backlogs. Depending on which backlog the user clicks and on which parent, a child item
     * will be created.
     *
     * @param title:              input for the title of the new task
     * @param description:        input for the description of the new task
     * @param acceptanceCriteria: under what conditions, is this task considered done
     * @param type:               input for the type of task
     * @param priority:           input for the priority of the task
     * @param effort:             input for the effort needed to complete the task
     * @param projectId:          project that this task belongs to
     * @param assigneeId:         id of the assigned user
     * @param parentId            : the parent of the task
     * @param session:            current session, needed to find the creator of this task
     * @return : returns a redirection to the project page
     * @throws ItemAlreadyExistsException : user has tried to create a task with the same title
     * @throws ItemHasEmptyTitleException : user has tried to create a task with no title
     */
    @PostMapping("/createItemOnTheGo")
    public String createItemOnTheGo(@RequestParam String title,
                                    @RequestParam String description,
                                    @RequestParam String acceptanceCriteria,
                                    @RequestParam String type,
                                    @RequestParam String priority,
                                    @RequestParam String effort,
                                    @RequestParam long projectId,
                                    @RequestParam long sprintId,
                                    @RequestParam long assigneeId,
                                    @RequestParam long parentId,
                                    @RequestParam long parentStatus,
                                    HttpSession session,
                                    RedirectAttributes redir) {
        try {
            //1 is for item with parent in project backlog
            if (parentStatus == 1) {
                itemService.createItemOnTheGo(null, title, description, acceptanceCriteria, ItemType.valueOf(type),
                        ItemPriority.valueOf(priority), effort, new Project(projectId),
                        new User(assigneeId), sessionService.getUserWithSessionId(session),
                        new Item(parentId));
            }
            //0 is for item without a parent in the sprint backlog, 2 for ready sprint and 3 for active
            else if (parentStatus == 0 || parentStatus == 2 || parentStatus == 3) {
                Optional<Sprint> sprintOptional = sprintService.findActiveSprintInProject(new Project(projectId));
                if (sprintOptional.isPresent()) {
                    Sprint sprint = sprintOptional.get();
                    Item item = itemService.createItemOnTheGo(sprint, title, description, acceptanceCriteria, ItemType.valueOf(type),
                            ItemPriority.valueOf(priority), effort, new Project(projectId),
                            new User(assigneeId), sessionService.getUserWithSessionId(session),
                            new Item(parentId));
                    itemSprintHistoryService.createAssociationAndSaveToRepository(item, new Sprint(sprintId));
                }
            }
            redir.addFlashAttribute("itemCreated", true);
        } catch (ItemAlreadyExistsException | ItemHasEmptyTitleException e) {
            //received full capitalized from front-end so we need to reformat it to first letter only capital
            String reformattedTypeStr = type.substring(0, 1) + type.substring(1).toLowerCase();
            redir.addFlashAttribute("createdItemOnTheGoTitle", title);
            redir.addFlashAttribute("createdItemOnTheGoDescription", description);
            redir.addFlashAttribute("createdItemOnTheGoAcceptanceCriteria", acceptanceCriteria);
            //same as above
            String reformattedPriorityStr = priority.substring(0, 1) + priority.substring(1).toLowerCase();
            redir.addFlashAttribute("createdItemOnTheGoPriority", reformattedPriorityStr);
            redir.addFlashAttribute("createdItemOnTheGoParentId", parentId);
            if (reformattedTypeStr.equals(ItemType.STORY.getName())) {
                redir.addFlashAttribute("createdItemOnTheGoModal", 2);
                redir.addFlashAttribute("createdItemOnTheGoBacklog", parentStatus);
                redir.addFlashAttribute("createdStoryItemParentId", parentId);
            } else if (reformattedTypeStr.equals(ItemType.TASK.getName()) || reformattedTypeStr.equals(ItemType.BUG.getName())) {
                redir.addFlashAttribute("createdItemOnTheGoModal", 1);
                redir.addFlashAttribute("createdItemOnTheGoBacklog", parentStatus);
                redir.addFlashAttribute("createdItemOnTheGoType", reformattedTypeStr);
                redir.addFlashAttribute("createdItemOnTheGoEffort", effort);
                redir.addFlashAttribute("createdItemOnTheGoAssigneeId", assigneeId);
            }
            if (e instanceof ItemAlreadyExistsException) {
                redir.addFlashAttribute("createdItemOnTheGoAlreadyExists", true);
            } else {
                redir.addFlashAttribute("createdItemOnTheGoHasEmptyTitle", true);
            }
        }
        return "redirect:/user/project/" + projectId;
    }

    /**
     * this method calls the item service in order to update an existing item and store it in the repository
     *
     * @param itemProjectId:          the id of the project, needed for the redirection
     * @param itemId:                 id of the item, needed to find it on the repository
     * @param itemTitle:              title of the item
     * @param itemDescription:        description of the item
     * @param itemAcceptanceCriteria: under what conditions, is this item considered done
     * @param itemType:               ItemType of the item
     * @param itemPriority:           ItemPriority of the item
     * @param itemEffort:             effort required to finish this item
     * @param itemEstimatedEffort:    estimated effort required to finish this item
     * @param itemAssigneeId:         the user that thisitem is assigned to
     * @param itemParentId            : the parent of the item
     * @param sprintId                : the sprint that this item belongs to
     * @param modifyItemPage:         allows the controller to know, which page this request comes from
     * @param redir:                  allows the controller to add 'flash' attributes, which will only be valid during redirection
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
    ) {
        //the associations need to be defined before the item is updated
        itemSprintHistoryService.manageItemSprintAssociation(new Item(itemId), new Sprint(sprintId), new Item(itemParentId));
        try {
            itemService.updateItem(itemId, itemTitle, itemDescription, itemAcceptanceCriteria, itemType, itemPriority,
                    itemEffort, itemEstimatedEffort,
                    new User(itemAssigneeId), new Item(itemParentId));
            redir.addFlashAttribute("itemUpdated", true);
        } catch (ItemAlreadyExistsException | ItemHasEmptyTitleException e) {
            redir.addFlashAttribute("failedToUpdateItem", itemId);
        }
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
        redir.addFlashAttribute("itemDeleted", true);
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

}

package gr.university.thesis.service;

import gr.university.thesis.entity.Item;
import gr.university.thesis.entity.ItemSprintHistory;
import gr.university.thesis.entity.Project;
import gr.university.thesis.entity.Sprint;
import gr.university.thesis.entity.enumeration.*;
import gr.university.thesis.exceptions.SprintHasZeroEffortException;
import gr.university.thesis.repository.SprintRepository;
import gr.university.thesis.util.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * service that is associated with the management of the sprints
 */
@Service
public class SprintService {

    ItemService itemService;
    SprintRepository sprintRepository;

    /**
     * constructor of this class, correct way to set the autowired attributes
     *
     * @param itemService:      service that manages items
     * @param sprintRepository: repository that has access to all sprints
     */
    @Autowired
    public SprintService(ItemService itemService, SprintRepository sprintRepository) {
        this.itemService = itemService;
        this.sprintRepository = sprintRepository;
    }

    /**
     * this method creates a sprint whenever it is called for a certain project,
     * and sets the sprint status to 2 (ready to be filled with items)
     *
     * @param project: the project that this sprint belongs to
     * @return : returns the sprint that was saved in the repository
     */
    public Sprint createSprint(Project project) {
        Sprint sprint = new Sprint(project, (byte) SprintStatus.READY.getRepositoryId());
        return sprintRepository.save(sprint);
    }

    /**
     * this method finds a sprint in the repository and returns it to the user
     *
     * @param sprint: the sprint that the user requested to find
     * @return : returns an optional that may contain a sprint
     */
    public Optional<Sprint> findSprintById(Sprint sprint) {
        return sprintRepository.findById(sprint.getId());
    }

    /**
     * this method searches the repository for the active sprint of the project that the user requested
     *
     * @param project: the project that the user requested to find the active sprint in
     * @return : returns an optional that may contain the requested sprint
     */
    public Optional<Sprint> findActiveSprintInProject(Project project) {
        Optional<Sprint> readySprintOptional = sprintRepository.findFirstByProjectAndStatus(project, (byte) SprintStatus.READY.getRepositoryId());
        //if there is a sprint ready to be filled with items
        if (readySprintOptional.isPresent()) {
            Sprint sprint = readySprintOptional.get();
            calculateTotalEffort(sprint);
            return readySprintOptional;
            //if there's no ready sprint but instead, an active one
        } else {
            Optional<Sprint> activeSprintOptional = sprintRepository.findFirstByProjectAndStatus(project, (byte) SprintStatus.ACTIVE.getRepositoryId());
            //if there is an active running sprint
            if (activeSprintOptional.isPresent()) {
                Sprint sprint = activeSprintOptional.get();
                calculateTotalEffort(sprint);

                int daysRemaining = Time.calculateDaysInBetween(new Date(), sprint.getEnd_date());
                sprint.setDays_remaining(daysRemaining);
                calculateVelocity(sprint);
                return activeSprintOptional;
            }
        }
        //if no active or ready sprint was found for some reason
        return readySprintOptional;
    }

    /**
     * this methods starts a sprint and moves it to an active state
     *
     * @param sprintId    : the sprint that the user wants to start
     * @param sprintGoal: the goal of the sprint, what do the users want to achieve by the time this sprint has
     *                    finished?
     * @throws SprintHasZeroEffortException : this exception is thrown when there are no tasks/bugs in the sprint, and the user
     *                                      * is trying to start it
     */
    public void startSprint(long sprintId, String sprintGoal) throws SprintHasZeroEffortException {
        Optional<Sprint> sprintOptional = findSprintById(new Sprint(sprintId));
        if (sprintOptional.isPresent()) {
            Sprint sprint = sprintOptional.get();
            calculateTotalEffort(sprint);
            if (sprint.getTotal_effort() == 0) {
                throw new SprintHasZeroEffortException("The sprint cannot have 0 total effort.");
            }
            sprint.setStatus((byte) SprintStatus.ACTIVE.getRepositoryId());
            Date now = new Date();
            sprint.setStart_date(now);
            int sprintDuration = sprint.getProject().getSprint_duration();
            Date endDate = Time.calculateEndDate(now, sprintDuration);
            sprint.setEnd_date(endDate);
            //example of input handling, not in the scope of this project
            if (sprintGoal.isEmpty()) {
                sprintGoal = "Goal not specified";
            } else {
                sprintGoal = sprintGoal.trim();
            }
            sprint.setGoal(sprintGoal);
            sprint.setDuration(sprintDuration);
            for (Item item : getAssociatedItemsList(sprint.getAssociatedItems())) {
                itemService.setStatusToItemAndChildren(item, ItemStatus.ACTIVE);
            }
            sprintRepository.save(sprint);
        }
    }

    /**
     * this methods finishes a sprint and moves it to a finish state
     *
     * @param sprintId: the sprint that the user wants to finish
     * @return : returns an optional with the new sprint (if everything went successfully)
     */
    public Optional<Sprint> finishSprint(long sprintId) {
        Optional<Sprint> sprintOptional = findSprintById(new Sprint(sprintId));
        if (sprintOptional.isPresent()) {
            Sprint sprint = sprintOptional.get();
            sprint.setStatus((byte) SprintStatus.FINISHED.getRepositoryId());
            sprint.setEnd_date(new Date());
            sprintOptional = Optional.of(sprintRepository.save(sprint));
            return sprintOptional;
        }
        return sprintOptional;
    }

    /**
     * this method takes as input associations between items and sprint, and returns in a list the set of items
     * from all the associations
     *
     * @param associations: the set of item-sprint history records that each one contains an item
     * @return : returns a list of items
     */
    public List<Item> getAssociatedItemsList(Set<ItemSprintHistory> associations) {
        List<Item> associatedItems = new ArrayList<>();
        if (associations != null) {
            for (ItemSprintHistory ish : associations) {
                associatedItems.add(ish.getItem());
            }
        }
        return associatedItems;
    }

    /**
     * this method finds a sprint in a certain project
     *
     * @param projectId: the project that the sprint belongs to
     * @param sprintId:  the sprint that the user wants to find
     * @return : returns an optional that may contain the sprint requested
     */
    public Optional<Sprint> findSprintByProjectId(long projectId, long sprintId) {
        return sprintRepository.findDistinctSprintByProjectId(projectId, sprintId);
    }

    /**
     * this method takes a sprint and calculates the total effort from all its children items within, it doesn't
     * take into account the stories/epics but rather, the children items of those parents (tasks/bugs etc.)
     * this method uses the item service to calculate the effort of each parent, before it sets the total effort
     * of the sprint
     *
     * @param sprint: the sprint that the total effort needs to be calculated
     */
    public void calculateTotalEffort(Sprint sprint) {
        itemService.calculatedCombinedEffort(getAssociatedItemsList(sprint.getAssociatedItems()));
        int totalEffort = 0;
        for (Item item : getAssociatedItemsList(sprint.getAssociatedItems())) {
            if (item.getType() == ItemType.TASK.getRepositoryId()
                    || item.getType() == ItemType.BUG.getRepositoryId()) {
                totalEffort += item.getEffort();
            }
        }
        sprint.setTotal_effort(totalEffort);
    }

    /**
     * this method takes as input a project and a sprint status, and searches the repository for all the
     * sprints with that certain status, mainly used to fetch all the finished sprints
     *
     * @param project:      the project that the user requested to see the sprints of
     * @param sprintStatus: the status of the sprints the user is looking for
     * @return : returns an optional that may contain a list of sprints with a certain status
     */
    public Optional<List<Sprint>> findSprintsByProjectAndStatus(Project project, SprintStatus sprintStatus) {
        Optional<List<Sprint>> finishedSprintsOptionals =
                sprintRepository.findSprintsByProjectAndStatusOrderByIdDesc(project, (byte) sprintStatus.getRepositoryId());
        if (finishedSprintsOptionals.isPresent()) {
            List<Sprint> finishedSprints = finishedSprintsOptionals.get();
            List<Sprint> sprintsWithoutTasks = new ArrayList<>();
            //getting the sprint ready before showing it
            for (Sprint sprint : finishedSprints) {
                boolean containsTasks = false;
                //if the sprint does not contain any tasks, then the sprint should be removed from this list, as it
                //will show an empty sprint in the sprint history page
                //this should be replaced with a more efficient algorithm, one for example which directly takes tasks/bugs
                //from the database, if the optional receiving is null, then skip etc.
                for (ItemSprintHistory ish : sprint.getAssociatedItems()) {
                    if (ish.getItem().getType() == ItemType.TASK.getRepositoryId()
                            || ish.getItem().getType() == ItemType.BUG.getRepositoryId()) {
                        containsTasks = true;
                        break;
                    }
                }
                if (!containsTasks) {
                    sprintsWithoutTasks.add(sprint);
                    continue;
                }
                calculateTotalEffort(sprint);
                calculateVelocity(sprint);
            }
            finishedSprints.removeAll(sprintsWithoutTasks);
        }
        return finishedSprintsOptionals;
    }

    /**
     * this method takes as input a sprint, and by adding all the effort from the items with type task or bug and
     * task board status of 'Done', it calculates the velocity of the sprint
     *
     * @param sprint: the sprint that the user requested to calculate the velocity of
     */
    public void calculateVelocity(Sprint sprint) {
        int totalVelocity = 0;
        for (ItemSprintHistory association : sprint.getAssociatedItems()) {
            if ((association.getItem().getType() == ItemType.TASK.getRepositoryId()
                    || association.getItem().getType() == ItemType.BUG.getRepositoryId())
                    && (association.getStatus() == TaskBoardStatus.DONE)) {
                totalVelocity += association.getItem().getEffort();
            }
        }
        sprint.setVelocity(totalVelocity);
    }

    /**
     * this method compares the sprint backlog and the project backlog, and notifies the user if more important items,
     * than the ones already existing in the sprint backlog, could be added to it
     * only the epics and stories are taken into the account and the tasks/bugs are ignored in the comparison
     * NOTE: this method fulfils the most common cases, but edge cases are ignored for example: only the most important
     * items are taken into consideration and not the second most important, which means that this method is incomplete
     *
     * @param sprint:         the sprint that the user wants to examine the backlog of
     * @param projectBacklog: the project backlog that needs to be examined and compared
     * @return: returns true if the sprint backlog contains the most important items, returns false if the sprint
     * backlog does not contain the most important items or at least one more important item can be added to it
     */
    public boolean findIfMostImportantItemsWereIncludedInSprint(Sprint sprint, Iterable projectBacklog) {
        ArrayList<Item> sprintBacklogParents = new ArrayList<>();
        ArrayList<Item> projectBacklogParents = new ArrayList<>();
        Set<ItemSprintHistory> associatedItems = sprint.getAssociatedItems();
        int mostImportantItemValueInSprintBacklog = 0;
        //will throw null exception
        if (associatedItems != null) {
            for (ItemSprintHistory itemSprintHistory : associatedItems) {
                //tasks are not included in this comparison, only epics and stories
                if (itemSprintHistory.getItem().getParent() == null
                        && (itemSprintHistory.getItem().getType() != ItemType.TASK.getRepositoryId()
                        && itemSprintHistory.getItem().getType() != ItemType.BUG.getRepositoryId())) {
                    sprintBacklogParents.add(itemSprintHistory.getItem());
                    if (itemSprintHistory.getItem().getPriority() > mostImportantItemValueInSprintBacklog) {
                        mostImportantItemValueInSprintBacklog = itemSprintHistory.getItem().getPriority();
                    }
                }
            }
        }
        //sprint backlog is null so why check, basically it is 'empty'
        else {
            return true;
        }
        int mostImportantItemValueInProjectBacklog = 0;
        for (Object objects : projectBacklog) {
            Item itemInProjectBacklog = (Item) objects;
            //again, tasks not included, only epics and stories
            if (itemInProjectBacklog.getType() == ItemType.EPIC.getRepositoryId() ||
                    (itemInProjectBacklog.getType() == ItemType.STORY.getRepositoryId() && itemInProjectBacklog.getParent() == null)) {
                projectBacklogParents.add(itemInProjectBacklog);
                if (itemInProjectBacklog.getPriority() > mostImportantItemValueInProjectBacklog) {
                    mostImportantItemValueInProjectBacklog = itemInProjectBacklog.getPriority();
                }
            }
        }
        if (mostImportantItemValueInSprintBacklog > mostImportantItemValueInProjectBacklog) {
            return true;
        } else if (mostImportantItemValueInSprintBacklog < mostImportantItemValueInProjectBacklog
                && mostImportantItemValueInSprintBacklog != 0) {
            return false;
        }
        //the following else clause is entirely based on the current total effort of the sprint
        else {
            int finalMostImportantItemValueInSprintBacklog = mostImportantItemValueInSprintBacklog;
            //removing all items from the list that are not 'the most important value'
            List<Item> mostImportantItemsInSprintBacklog =
                    sprintBacklogParents.stream().filter(item -> item.getPriority() ==
                            ItemPriority.findItemPriorityByRepositoryId(finalMostImportantItemValueInSprintBacklog)
                                    .getRepositoryId()).collect(Collectors.toList());
            //calculating the effort of the remaining parents
            int effortOfRemainingParents = mostImportantItemsInSprintBacklog.stream().mapToInt(item -> item.getEffort()).sum();
            int sprintTotalEffort = sprintBacklogParents.stream().mapToInt(item -> item.getEffort()).sum();

            for (Item projectBacklogParent : projectBacklogParents) {
                if (projectBacklogParent.getPriority() == finalMostImportantItemValueInSprintBacklog) {
                    effortOfRemainingParents += projectBacklogParent.getEffort();
                    //if the sprint backlog can fit at least one more 'most important item' from the product backlog,
                    // then notify the user that they should remove the 'least important items' and add
                    // a more 'most important item' instead
                    if (effortOfRemainingParents < sprintTotalEffort) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    /**
     * this method saves in the repository the sprint that the user requested to be saved and returns the sprint with
     * the newly added unique repository id
     *
     * @param sprint: the sprint that the user requested to save
     * @return: returns the sprint saved in the repository, including the id the repository generated for this object
     */
    public Sprint save(Sprint sprint) {
        return sprintRepository.save(sprint);
    }
}

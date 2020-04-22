package gr.university.thesis.service;

import gr.university.thesis.dto.BurnDownChartData;
import gr.university.thesis.dto.TasksDoneByDate;
import gr.university.thesis.entity.Item;
import gr.university.thesis.entity.ItemSprintHistory;
import gr.university.thesis.entity.Sprint;
import gr.university.thesis.entity.enumeration.ItemStatus;
import gr.university.thesis.entity.enumeration.ItemType;
import gr.university.thesis.entity.enumeration.SprintStatus;
import gr.university.thesis.entity.enumeration.TaskBoardStatus;
import gr.university.thesis.repository.ItemSprintHistoryRepository;
import gr.university.thesis.util.Time;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * service that handles everything relating to the history between the items and the sprints stored in the repository
 */
@Service
public class ItemSprintHistoryService {

    ItemService itemService;
    SprintService sprintService;
    ItemSprintHistoryRepository itemSprintHistoryRepository;

    /**
     * constructor of this class, correct way to set the autowired attributes
     *
     * @param itemService:                 service that manages items
     * @param sprintService:               service that manages sprints
     * @param itemSprintHistoryRepository: repository for the history of item/sprint
     */
    @Autowired
    public ItemSprintHistoryService(ItemService itemService, SprintService sprintService,
                                    ItemSprintHistoryRepository itemSprintHistoryRepository) {
        this.itemService = itemService;
        this.sprintService = sprintService;
        this.itemSprintHistoryRepository = itemSprintHistoryRepository;
    }

    /**
     * this method creates an association between a sprint and an item, and stores it in the repository with a
     * TO_DO status
     *
     * @param item:   the item that the association is created for
     * @param sprint: the sprint that this association belongs to
     */
    public void createAssociationAndSaveToRepository(Item item, Sprint sprint) {
        ItemSprintHistory ItemSprintHistory = new ItemSprintHistory(item, sprint,
                TaskBoardStatus.TO_DO);
        itemSprintHistoryRepository.save(ItemSprintHistory);
    }

    /**
     * this method moves an item to a sprint, and creates the necessary associations in the item-sprint history table in
     * the repository
     *
     * @param item:   the item the user wants to move into a sprint
     * @param sprint: the sprint that the user wants to move an item into
     * @param parent: the parent of the item, needed to define whether new associations should be created or removed
     */
    public void moveItemToSprint(Item item, Sprint sprint, Item parent) {
        Optional<Item> itemOptional = itemService.findItemById(item);
        Optional<Sprint> sprintOptional = sprintService.findSprintById(sprint);
        if (itemOptional.isPresent() && sprintOptional.isPresent()) {
            item = itemOptional.get();
            sprint = sprintOptional.get();
            itemService.setStatusToItemAndChildren(item, ItemStatus.READY);
            //whatever the user clicks to move to the sprint, will have no parent
            if (parent == null) {
                item.setParent(null);
            }
            //if the user changes the parent to a parent that is in the ready sprint, then we need to set it this way:
            else {
                item.setParent(parent);
            }
            itemService.saveToRepository(item);
            List<ItemSprintHistory> assocations = createAssociationForItemAndChildren(item, sprint);
            itemSprintHistoryRepository.saveAll(assocations);
        }
    }

    /**
     * this method removes an item from a sprint, and deletes the existing associations in the item-sprint history
     * table in the repository
     *
     * @param item:   the item the user wants to remove from a sprint
     * @param sprint: the sprint that the user wants to remove an item from
     * @param parent: the parent of the item, needed to define whether new associations should be created or removed
     */
    public void removeItemFromSprint(Item item, Sprint sprint, Item parent) {
        Optional<Item> itemOptional = itemService.findItemById(item);
        Optional<Sprint> sprintOptional = sprintService.findSprintById(sprint);
        if (itemOptional.isPresent() && sprintOptional.isPresent()) {
            item = itemOptional.get();
            sprint = sprintOptional.get();
            Optional<ItemSprintHistory> itemSprintHistoryOptional = itemSprintHistoryRepository.findFirstByItemAndSprint(item,
                    sprint);
            if (itemSprintHistoryOptional.isPresent()) {
                itemSprintHistoryRepository.delete(itemSprintHistoryOptional.get());
                itemService.setStatusToItemAndChildren(item, ItemStatus.BACKLOG);
                //whatever the user clicks to move to the sprint, will have no parent
                if (parent == null) {
                    item.setParent(null);
                }
                //if the user changes the parent to a parent that is in the ready sprint, then we need to set it this way:
                else {
                    item.setParent(parent);
                }
                itemService.saveToRepository(item);
                List<ItemSprintHistory> associations = createAssociationForItemAndChildren(item, sprint);
                itemSprintHistoryRepository.deleteAll(associations);
            }
        }
    }

    /**
     * this method saves a batch of associations into the repository, mainly used to avoid making many calls to the
     * repository
     *
     * @param itemSprintHistories: the list of associations that the user requested to save
     */
    public void saveAllAssociations(List<ItemSprintHistory> itemSprintHistories) {
        itemSprintHistoryRepository.saveAll(itemSprintHistories);
    }

    /**
     * this method takes as input an association and saves it into the repository, and then returns the recently
     * saved association with the addition of the id the repository generated for it
     *
     * @param itemSprintHistory: the association the user requested to save
     * @return: returns the association with the addition of the id the repository generated for it
     */
    public ItemSprintHistory save(ItemSprintHistory itemSprintHistory) {
        return itemSprintHistoryRepository.save(itemSprintHistory);
    }

    /**
     * this method creates associations between an item and its children with a sprint, and returns a list of
     * associations that will be stored in the item-sprint-history table in the repository
     *
     * @param item:   the item(possibly parent) that we want to create an association for
     * @param sprint: the sprint that belongs to the assocation
     * @return : returns a list with all the associations created in this method
     */
    private List<ItemSprintHistory> createAssociationForItemAndChildren(Item item, Sprint sprint) {
        List<ItemSprintHistory> associations = new ArrayList<>();
        ItemSprintHistory parentItemSprintHistory = new ItemSprintHistory(item, sprint,
                TaskBoardStatus.TO_DO);
        parentItemSprintHistory.getSprintItemId().setItemId(item.getId());
        parentItemSprintHistory.getSprintItemId().setSprintId(sprint.getId());
        associations.add(parentItemSprintHistory);
        //if it's a story or an epic, then create an association for the children
        if (item.getType() == ItemType.EPIC.getRepositoryId() || item.getType() == ItemType.STORY.getRepositoryId()) {
            if (item.getChildren() != null) {
                for (Item child : item.getChildren()) {
                    if (child.getStatus() != ItemStatus.FINISHED.getRepositoryId()) {
                        ItemSprintHistory childItemSprintHistory = new ItemSprintHistory(child, sprint,
                                TaskBoardStatus.TO_DO);
                        childItemSprintHistory.getSprintItemId().setItemId(child.getId());
                        childItemSprintHistory.getSprintItemId().setSprintId(sprint.getId());
                        associations.add(childItemSprintHistory);
                        //if the parent is an epic and the child a story, then also create an association for the
                        // children of the story
                        for (Item childOfChild : child.getChildren()) {
                            if (childOfChild.getStatus() != ItemStatus.FINISHED.getRepositoryId()) {
                                ItemSprintHistory childOfChildItemSprintHistory = new ItemSprintHistory(childOfChild, sprint,
                                        TaskBoardStatus.TO_DO);
                                childOfChildItemSprintHistory.getSprintItemId().setSprintId(sprint.getId());
                                childOfChildItemSprintHistory.getSprintItemId().setItemId(childOfChild.getId());
                                associations.add(childOfChildItemSprintHistory);
                            }
                        }
                    }
                }
            }
        }
        return associations;
    }

    /**
     * this method checks if an item needs to have its associations updated when the user requests to
     * assign it to a new parent, for example if the item is in the backlog and needs to be set
     * under a parent that is in the ready sprint, then new associations need to be created in the
     * item-sprint-history table for that specific item and its children
     *
     * @param item:   the item that we want to check if association updating is needed
     * @param sprint: the sprint that the item belongs to
     * @param parent: the new parent of the sprint
     */
    public void manageItemSprintAssociation(Item item, Sprint sprint, Item parent) {
        Optional<Item> itemOptional = itemService.findItemById(item);
        Optional<Sprint> sprintOptional = sprintService.findSprintById(sprint);
        Optional<Item> parentOptional;
        if (parent.getId() != 0) {
            parentOptional = itemService.findItemById(parent);
        } else {
            return;
        }
        if (itemOptional.isPresent() && sprintOptional.isPresent() && (parentOptional != null && parentOptional.isPresent())) {
            item = itemOptional.get();
            sprint = sprintOptional.get();
            parent = parentOptional.get();
            if (item.getStatus() == 1 && parent.getStatus() == 1) {
                return;
            } else if (item.getStatus() == 1 && parent.getStatus() == 2) {
                moveItemToSprint(item, sprint, parent);
            } else if (item.getStatus() == 2 && parent.getStatus() == 1) {
                removeItemFromSprint(item, sprint, parent);
            }
        }
    }

    /**
     * this method takes as input two types (at max) of item (bug and task for example), and returns all the items
     * that belongs to a certain sprint and have a certain task board status (for example 'done'), the developer chose
     * two item types, because at the time of creating this method, they needed a way to fetch both tasks and bugs,
     * and instead of using 2 queries for it, they used only one
     *
     * @param sprint:          the sprint that the requested items belong to
     * @param taskBoardStatus: the task board status of the requested items (for example 'done')
     * @param itemType1:       item type 1 (for example bug)
     * @param itemType2:       item type 2 (for example task)
     * @return : returns an optional that may contain a list with all the items requested
     */
    public Optional<List<ItemSprintHistory>> findAllAssociationsByStatus(Sprint sprint, TaskBoardStatus taskBoardStatus, ItemType itemType1, ItemType itemType2) {
        return itemSprintHistoryRepository.findAllBySprintAndStatusAndItemTypes(sprint.getId(),
                taskBoardStatus, itemType1.getRepositoryId(), itemType2.getRepositoryId());
    }

    /**
     * this method takes as input a sprint, and two types and returns all the items that belong to any of the two
     * types inputted and belong to that sprint
     *
     * @param sprint:    the sprint that the user requested to see the items of
     * @param itemType1: item type 1 (for example bug)
     * @param itemType2: item type 2 (for example task)
     * @return: returns an optional that may contain a list with all the items requested
     */
    public Optional<List<ItemSprintHistory>> findAllAssociationsBySprintAndTypes(Sprint sprint, ItemType itemType1, ItemType itemType2) {
        return itemSprintHistoryRepository.findAllBySprintAndItemTypes(sprint.getId(), itemType1.getRepositoryId(), itemType2.getRepositoryId());
    }

    /**
     * this method changes the status of a given association, either to the previous or to the next and the direction
     * is indicated by the index
     *
     * @param sprint: the sprint that belongs to this association
     * @param item:   the item that belongs to this association
     * @param index:  the indicator towards which direction the status should go to, if index > 0 then it means that
     *                the status should be increased by 1, whereas if index < 0 then it means that the status should be
     *                decreased by one (based on the repository id)
     */
    public void changeStatusOfAssociationByOne(Sprint sprint, Item item, int index) {
        Optional<ItemSprintHistory> itemSprintHistoryOptional = itemSprintHistoryRepository.findFirstByItemAndSprint(item, sprint);
        if (itemSprintHistoryOptional.isPresent()) {
            ItemSprintHistory itemSprintHistory = itemSprintHistoryOptional.get();
            //checking for edge cases
            if ((itemSprintHistory.getStatus().getRepositoryId() != 1 && index < 0) ||
                    (itemSprintHistory.getStatus().getRepositoryId() != 4 && index > 0)) {
                TaskBoardStatus currentStatus = itemSprintHistory.getStatus();
                itemSprintHistory.setStatus(TaskBoardStatus.findTaskBoardStatusByRepositoryId(currentStatus.getRepositoryId() + index));
                itemSprintHistory.setLast_moved(new Date());
                itemSprintHistoryRepository.save(itemSprintHistory);
            }
        }
    }

    /**
     * this method transfers all the unfinished items from the old sprint to the new sprint, this means that all
     * associations with task board status that 'DONE', will not be transferred whereas all the other associations,
     * will be transferred with the status that they were left, when the sprint ended
     *
     * @param oldSprint: the old sprint that we want to transfer the associations from
     */
    public void transferUnfinishedItemsFromOldSprint(Sprint oldSprint) {
        Sprint newSprint = sprintService.createSprint(oldSprint.getProject());
        Set<ItemSprintHistory> associationsForNextSprint = new HashSet<>();
        for (ItemSprintHistory association : oldSprint.getAssociatedItems()) {
            //if the item is a task or a bug and it was not finished in the old sprint, then move it to the new one
            if ((association.getItem().getType() == ItemType.TASK.getRepositoryId() ||
                    association.getItem().getType() == ItemType.BUG.getRepositoryId()) &&
                    association.getStatus() != TaskBoardStatus.DONE &&
                    association.getItem().getStatus() != ItemStatus.FINISHED.getRepositoryId()) {
                associationsForNextSprint.addAll(createAssociationForItemAndParents(association.getItem(),
                        newSprint, association.getStatus()));
            }
            //done status can only be tasks/bugs, so no need to check if it's an epic/story etc.
            else if (association.getStatus() == TaskBoardStatus.DONE) {
                itemService.changeItemStatus(association.getItem(), ItemStatus.FINISHED);
                boolean isParentComplete = false;
                if (association.getItem().getParent() != null) {
                    isParentComplete = itemService.checkIfParentIsComplete(association.getItem());
                }
                //if the parent of the child is complete, then change the parent's state to 'finished' as well
                if (isParentComplete) {
                    itemService.changeItemStatus(association.getItem().getParent(), ItemStatus.FINISHED);
                    if (association.getItem().getParent().getParent() != null) {
                        boolean isParentOfParentComplete = itemService.checkIfParentIsComplete(association.getItem().getParent().getParent());
                        //if the parent of the parent is complete, then also change his state to 'finished'
                        if (isParentOfParentComplete) {
                            itemService.changeItemStatus(association.getItem().getParent().getParent(), ItemStatus.FINISHED);
                        }
                    }
                }
            }
            // in case the item has no children, this case is only added to avoid breaking the system with
            //stories or epics that are added onto the ready sprint without children
            //however this edge case is not perfect as an epic/story can still be added
            //to the ready sprint and have children that have been finished and cannot be seen
            //by the user, but as of now, entirely solving the edge case is not of primary concern
            else if ((association.getItem().getType() == ItemType.EPIC.getRepositoryId() ||
                    association.getItem().getType() == ItemType.STORY.getRepositoryId()) &&
                    association.getItem().getChildren().isEmpty()) {
                associationsForNextSprint.addAll(createAssociationForItemAndParents(association.getItem(),
                        newSprint, association.getStatus()));
            }
        }
        itemSprintHistoryRepository.saveAll(associationsForNextSprint);
    }

    /**
     * this method creates associations for an item and its parents, for example if a task needs to be transferred
     * to a new sprint, creating associations for the story that it might belong to, is also needed. What is more, if
     * the story has its own parent, then an association for that parent (epic) needs to also be created
     *
     * @param item:      the item that the user requested to create an association for it and its parents
     * @param newSprint: the new sprint that these items are going to be associated with
     * @param oldStatus: the old status, from when the previous sprint ended, for the item to inherit
     * @return : returns a set with the unique associations that were created
     */
    public Set<ItemSprintHistory> createAssociationForItemAndParents(Item item, Sprint newSprint, TaskBoardStatus oldStatus) {
        //the developer chose Set as their data structure here, because they want to avoid having duplicates
        //inserted into the repository, two items may have the same parent, and two associations with the same
        //parent ID and sprint ID will be created, which is unwanted
        Set<ItemSprintHistory> associations = new HashSet<>();
        item = itemService.changeItemStatus(item, ItemStatus.READY);
        ItemSprintHistory newAssociation = new ItemSprintHistory(item, newSprint, oldStatus);
        associations.add(newAssociation);
        if (item.getParent() != null) {
            Item parent = item.getParent();
            parent = itemService.changeItemStatus(parent, ItemStatus.READY);
            if (parent.getType() == ItemType.EPIC.getRepositoryId()) {
                ItemSprintHistory newParentAssociation = new ItemSprintHistory(parent, newSprint, TaskBoardStatus.NONE);
                associations.add(newParentAssociation);
            }
            //different case is needed for stories, as they might have a parent on their own
            else if (parent.getType() == ItemType.STORY.getRepositoryId()) {
                ItemSprintHistory newParentAssociation = new ItemSprintHistory(parent, newSprint, TaskBoardStatus.NONE);
                associations.add(newParentAssociation);
                //if the story has an epic as its parent
                if (parent.getParent() != null) {
                    Item parentOfParent = parent.getParent();
                    parentOfParent = itemService.changeItemStatus(parentOfParent, ItemStatus.READY);
                    ItemSprintHistory newParentOfParentAssociation = new ItemSprintHistory(parentOfParent, newSprint, TaskBoardStatus.NONE);
                    associations.add(newParentOfParentAssociation);
                }
            }
        }
        return associations;
    }

    /**
     * this method calculates all the necessary data for the burn down chart in the history page of every sprint
     * it calculates the duration of the sprint in days and puts them in a ordered array named categories
     * it calculates the ideal burn of effort by dividing the total sprint effort with the number of developers
     * included in the project, and puts every ideal burn of effort per day in an array
     * it calculates the actual burn of effort by calculating all the tasks done per day and puts them in an array
     *
     * @param sprint: the sprint that the user requested to calculate the burn down chart data of
     * @return : returns a DTO that contains all the necessary data that need to be transferred to the user interface\
     * and concern the burndown chart
     */
    public BurnDownChartData calculateBurnDownChartData(Sprint sprint) {
        Optional<Sprint> sprintOptional = sprintService.findSprintById(sprint);
        if (sprintOptional.isPresent()) {
            sprint = sprintOptional.get();
            sprintService.calculateTotalEffort(sprint);
            int totalSprintEffort = (int) sprint.getTotal_effort();

            //number of weeks * 7
            int numberOfDays = (int) sprint.getDuration() * 7;
            //for readability issues
            Date startingDate = sprint.getStart_date();
            Date nextDate = startingDate;

            //+2 because the start and finish are included
            Date[] dates = new Date[numberOfDays + 2];
            dates[0] = startingDate;
            //counting the Start string and the last Day (Wed)
            String[] categories = new String[numberOfDays + 2];
            //setting the starting cell manually
            categories[0] = "Start";
            for (int i = 1; i < categories.length; i++) {
                String nextDay = Time.findDay(nextDate);
                categories[i] = nextDay;
                nextDate = Time.incrementDateByOne(nextDate);
                dates[i] = nextDate;
            }
            //outside the loop, setting the last cell manually
            categories[numberOfDays + 1] += "(Finish)";

            int[] actualBurn = new int[numberOfDays + 2];
            actualBurn[0] = totalSprintEffort;
            Optional<List<ItemSprintHistory>> associationsOrderedByDateOptional =
                    findAllAssociationsByStatus(sprint, TaskBoardStatus.DONE, ItemType.TASK, ItemType.BUG);
            if (associationsOrderedByDateOptional.isPresent()) {
                List<ItemSprintHistory> associationsOrderedByDate = associationsOrderedByDateOptional.get();

                int currentActualBurn = 0;

                //for readability issues
                int previousEffort = totalSprintEffort;
                // so that it is not instantiated every time inside the loop
                Date today = new Date();
                for (int i = 0; i < dates.length; i++) {
                    Date date = dates[i];
                    //if the date comparing is later than today
                    if (Time.compare(date, today) > 0) {
                        //only take the burns of earlier dates and today
                        actualBurn = Arrays.copyOfRange(actualBurn, 0, i + 1);
                        break;
                    } else {
                        for (ItemSprintHistory association : associationsOrderedByDate) {
                            int difference = Time.compare(date, association.getLast_moved());
                            //if the dates are equal
                            if (difference == 0) {
                                currentActualBurn += association.getItem().getEffort();
                            }
                            //if the association is later, then break as there is no need to check further in the list
                            //as the associations are ordered by date: if the association date is later, then surely the
                            //next date is also later and so on
                            else if (difference < 0) {
                                break;
                            }
                        }
                    }
                    //if the current actual burn is not zero, it means that finished associations were found for that day
                    if (currentActualBurn != 0) {
                        actualBurn[i + 1] = previousEffort - currentActualBurn;
                        //if the burn is below 0, then set it to 0, as the graph should never show below 0
                        // (just making sure)
                        if (actualBurn[i + 1] <= 0) {
                            actualBurn[i + 1] = 0;
                            //actually ending the actual remaining graph to the point it reaches 0
                            actualBurn = Arrays.copyOfRange(actualBurn, 0, i + 2);
                            break;
                        }
                        //setting the effort, to the current iterated cell
                        previousEffort = actualBurn[i + 1];
                        //resetting it for next set of associations
                        currentActualBurn = 0;
                    }
                    //if the date checked had no associations with actual burns for that day, for example Saturday/Sunday
                    else if (Time.compare(date, today) <= 0) {
                        //set the effort for that cell, the same as the previous, for example the same as Friday (as no
                        //tasks were completed during off days)
                        actualBurn[i + 1] = previousEffort;
                    }
                }
            }
            //if there are no tasks with status done, just take the first cell, which is the total effort of
            //the sprint and fill it up to the latest day
            else {
                //+1 for the starting cell
                //+1 for the return of the method which is int and normally should be double
                //may be slightly inaccurate if compared to the current hour, but the days shown display based on the
                //starting date of the sprint
                int daysInBetween = Time.calculateDaysInBetween(sprint.getStart_date(), new Date()) + 1 + 1;
                //this is the point that the array will be 'cut', based on the current day (now)
                int arrayLimit = 0;
                for (int i = 0; i < daysInBetween; i++) {
                    //in case there is a out of bounds exception
                    if (i < actualBurn.length) {
                        actualBurn[i] = totalSprintEffort;
                        arrayLimit = i + 1;
                    }
                    //if let's say 500 days have passed, the iteration should break and not count more than needed
                    if (i >= actualBurn.length) {
                        break;
                    }
                }
                actualBurn = Arrays.copyOfRange(actualBurn, 0, arrayLimit);
            }

            //here, the ideal burn for each sprint is calculated
            double[] ideal_burn = new double[numberOfDays + 2];
            ideal_burn[0] = totalSprintEffort;
            double nextEffort = totalSprintEffort;
            //double needed because the division might be a decimal, and an accurate representation is required
            double ideal_effort_burn = (double) totalSprintEffort / (numberOfDays + 1);
            //limiting decimals to two:
            DecimalFormat df = new DecimalFormat("#.##");
            df.setRoundingMode(RoundingMode.HALF_EVEN);

            for (int i = 1; i < ideal_burn.length; i++) {
                //formatting returns a String, so parsing is needed
                ideal_burn[i] = Double.parseDouble(df.format(nextEffort - ideal_effort_burn));
                //here, having a number below 0 is possible, so it should be avoided
                if (ideal_burn[i] < 0) {
                    ideal_burn[i] = 0;
                }
                //this should also be called previous effort (most likely), setting the effort calculated for the
                // previous cell, for the next subtraction
                nextEffort = ideal_burn[i];
            }
            boolean possibleDelay = false;
            boolean earlierFinish = false;
            //if the sprint is not the current active one, then there is no point in warning the user

            if (sprint.getStatus() == SprintStatus.ACTIVE.getRepositoryId()) {
                //if the latest actual burn is more than the ideal burn of that day, then there is a problem
                //and the user needs to be warned
                if (actualBurn[actualBurn.length - 1] > ideal_burn[actualBurn.length - 1]) {
                    possibleDelay = true;
                }
                //if the actual burn reaches 0 and the length of the actual burn array is shorter than the ideal burn
                //array (which means the former reaches 0 first), then notify user about early finish
                if (actualBurn[actualBurn.length - 1] == 0 && actualBurn.length < ideal_burn.length) {
                    earlierFinish = true;
                }

            }
            return new BurnDownChartData(categories, ideal_burn, actualBurn, possibleDelay, earlierFinish);
        }
        return null;
    }

    /**
     * this method takes as input a sprint, and collects all its item of every association, and groups it by
     * date using a map, then the map is converted into a DTO containing the list of items grouped by date,
     * and are then shown in the user interface during the sprint task history template
     * the map could have been returned but for readability reasons, the author chose to translate it into a DTO
     *
     * @param sprint: the sprint that the user requested to sort the tasks of and show to the sprint history
     * @return : returns a list of DTOs, that contain all the items, grouped by date
     */
    public List<TasksDoneByDate> sortTasksByDate(Sprint sprint) {
        Optional<List<ItemSprintHistory>> associationsByStatusOptional = findAllAssociationsByStatus(sprint, TaskBoardStatus.DONE, ItemType.TASK, ItemType.BUG);
        if (associationsByStatusOptional.isPresent()) {
            List<ItemSprintHistory> associationsByStatus = associationsByStatusOptional.get();
            //need to set all clocks to 00:00:00 so that it is easier to group by date
            for (ItemSprintHistory association : associationsByStatus) {
                Date newDate = Time.setClockToZero(association.getLast_moved());
                association.setLast_moved(newDate);
            }
            //lambda expression is then used to group items by date, and store them into a map
            Map<Date, List<ItemSprintHistory>> groupedByDate = associationsByStatus.stream()
                    .collect(Collectors.groupingBy(ItemSprintHistory::getLast_moved));
            List<TasksDoneByDate> tasksDoneByDatesList = new ArrayList<>(groupedByDate.size());
            //converting the map into a DTO, mainly for readability issues during code review
            for (Map.Entry<Date, List<ItemSprintHistory>> entry : groupedByDate.entrySet()) {
                TasksDoneByDate tasksDoneByDate = new TasksDoneByDate();
                tasksDoneByDate.setDate(entry.getKey());
                List<Item> items = sprintService.getAssociatedItemsList(new HashSet<>(entry.getValue()));
                tasksDoneByDate.setItems(items);
                tasksDoneByDatesList.add(tasksDoneByDate);
            }
            //sort the dates again because the map entries return a set
            Collections.sort(tasksDoneByDatesList, Comparator.comparing(TasksDoneByDate::getDate));
            return tasksDoneByDatesList;
        }
        return null;
    }
}
